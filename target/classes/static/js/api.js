/**
 * The frontend's only HTTP client. Keep network requests and API error
 * normalization here so page modules can focus on their own behaviour.
 */
import { API_BASE_URL, STORAGE_KEYS } from './config.js';

const AUTH_PATHS = ['/auth/login', '/auth/register'];
const SAFE_RETRY_MESSAGE = 'We could not reach EventSaathi. Check your connection and try again.';

export class ApiError extends Error {
  constructor(message, { status = 0, fieldErrors = null, raw = null, isNetworkError = false } = {}) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.fieldErrors = fieldErrors;
    this.raw = raw;
    this.isNetworkError = isNetworkError;
  }
}

function validationErrors(responseData) {
  if (responseData?.errors && !Array.isArray(responseData.errors)) {
    return responseData.errors;
  }

  const message = responseData?.message;
  if (typeof message !== 'string') return null;

  const fields = {};
  for (const entry of message.split(/;\s*/)) {
    const match = entry.match(/^([A-Za-z][\w.[\]]*):\s*(.+)$/);
    if (match) fields[match[1]] = match[2];
  }
  return Object.keys(fields).length ? fields : null;
}

async function readResponse(response) {
  if (response.status === 204 || response.headers.get('content-length') === '0') return null;

  const contentType = response.headers.get('content-type') || '';
  if (contentType.includes('application/json')) {
    try {
      return await response.json();
    } catch {
      return null;
    }
  }

  const text = await response.text();
  return text || null;
}

function redirectForExpiredSession(endpoint) {
  if (AUTH_PATHS.some(path => endpoint.startsWith(path))) return;

  const isAuthPage = /\/pages\/(?:login|register)\.html$/.test(window.location.pathname);
  if (isAuthPage) return;

  localStorage.removeItem(STORAGE_KEYS.TOKEN);
  localStorage.removeItem(STORAGE_KEYS.USER);
  localStorage.setItem(STORAGE_KEYS.REDIRECT_URL, window.location.href);
  localStorage.setItem(STORAGE_KEYS.SESSION_FLASH, JSON.stringify({
    type: 'warning',
    title: 'Session expired',
    message: 'Session expired, please log in again.'
  }));

  const pageDirectory = new URL('../pages/', import.meta.url);
  window.location.assign(new URL('login.html', pageDirectory).href);
}

class ApiClient {
  constructor(baseUrl) {
    this.baseUrl = baseUrl.replace(/\/+$/, '');
  }

  async request(endpoint, options = {}) {
    const path = endpoint.startsWith('/') ? endpoint : `/${endpoint}`;
    const url = /^https?:\/\//i.test(path) ? path : `${this.baseUrl}${path}`;
    const headers = new Headers(options.headers || {});
    headers.set('Accept', 'application/json');

    const token = localStorage.getItem(STORAGE_KEYS.TOKEN);
    if (token) headers.set('Authorization', `Bearer ${token}`);

    let body = options.body;
    if (body !== undefined && body !== null) {
      const isFormData = typeof FormData !== 'undefined' && body instanceof FormData;
      if (!isFormData && typeof body !== 'string') {
        headers.set('Content-Type', 'application/json');
        body = JSON.stringify(body);
      }
    }

    let response;
    try {
      response = await fetch(url, { ...options, body, headers });
    } catch {
      throw new ApiError(SAFE_RETRY_MESSAGE, { isNetworkError: true });
    }

    const responseData = await readResponse(response);

    if (response.status === 401) {
      redirectForExpiredSession(path);
      throw new ApiError(responseData?.message || 'Please check your email and password, then try again.', {
        status: 401,
        raw: responseData
      });
    }

    if (response.status === 403) {
      throw new ApiError("You don't have access to this action.", {
        status: 403,
        raw: responseData
      });
    }

    if (!response.ok) {
      const message = response.status >= 500
        ? 'Something went wrong on our side. Please try again in a moment.'
        : responseData?.message || responseData?.error || `Request could not be completed (${response.status}).`;

      throw new ApiError(message, {
        status: response.status,
        fieldErrors: response.status === 400 ? validationErrors(responseData) : null,
        raw: responseData
      });
    }

    return responseData;
  }

  get(endpoint, options = {}) {
    return this.request(endpoint, { ...options, method: 'GET' });
  }

  post(endpoint, body, options = {}) {
    return this.request(endpoint, { ...options, method: 'POST', body });
  }

  put(endpoint, body, options = {}) {
    return this.request(endpoint, { ...options, method: 'PUT', body });
  }

  patch(endpoint, body, options = {}) {
    return this.request(endpoint, { ...options, method: 'PATCH', body });
  }

  delete(endpoint, options = {}) {
    return this.request(endpoint, { ...options, method: 'DELETE' });
  }
}

export const api = new ApiClient(API_BASE_URL);
export default api;

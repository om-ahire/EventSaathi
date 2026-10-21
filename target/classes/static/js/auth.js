/**
 * Session storage, authentication requests, and client-side role guards.
 * The backend remains responsible for enforcing authorization.
 */
import { api } from './api.js';
import { ROLES, STORAGE_KEYS } from './config.js';

function saveSession(response) {
  if (!response?.token || !response?.user) {
    throw new Error('The server response did not include a valid sign-in session.');
  }

  localStorage.setItem(STORAGE_KEYS.TOKEN, response.token);
  localStorage.setItem(STORAGE_KEYS.USER, JSON.stringify(response.user));
  return response.user;
}

function pageUrl(path) {
  const inPages = window.location.pathname.includes('/pages/');
  return new URL(inPages ? `../${path}` : path, window.location.href).href;
}

function saveFlash(type, title, message) {
  localStorage.setItem(STORAGE_KEYS.SESSION_FLASH, JSON.stringify({ type, title, message }));
}

export const auth = {
  async login(email, password) {
    return saveSession(await api.post('/auth/login', { email, password }));
  },

  async register({ name, email, password, phone = '' }) {
    return saveSession(await api.post('/auth/register', { name, email, password, phone }));
  },

  logout(redirectPath) {
    localStorage.removeItem(STORAGE_KEYS.TOKEN);
    localStorage.removeItem(STORAGE_KEYS.USER);
    localStorage.removeItem(STORAGE_KEYS.REDIRECT_URL);
    window.location.assign(redirectPath || pageUrl('index.html'));
  },

  getToken() {
    return localStorage.getItem(STORAGE_KEYS.TOKEN);
  },

  getCurrentUser() {
    const storedUser = localStorage.getItem(STORAGE_KEYS.USER);
    if (!storedUser) return null;

    try {
      return JSON.parse(storedUser);
    } catch {
      localStorage.removeItem(STORAGE_KEYS.USER);
      return null;
    }
  },

  isAuthenticated() {
    return Boolean(this.getToken() && this.getCurrentUser());
  },

  hasRole(role) {
    const roles = this.getCurrentUser()?.roles;
    if (!Array.isArray(roles)) return false;
    const expectedRole = role.startsWith('ROLE_') ? role : `ROLE_${role}`;
    return roles.includes(expectedRole);
  },

  async refreshUser() {
    if (!this.isAuthenticated()) return null;
    const user = await api.get('/auth/me');
    if (!user) throw new Error('The server did not return the signed-in user profile.');
    localStorage.setItem(STORAGE_KEYS.USER, JSON.stringify(user));
    return user;
  },

  requireAuth(allowedRoles = []) {
    if (!this.isAuthenticated()) {
      localStorage.setItem(STORAGE_KEYS.REDIRECT_URL, window.location.href);
      saveFlash('warning', 'Sign in to continue', 'Please log in to access this page.');
      window.location.assign(pageUrl('pages/login.html'));
      return false;
    }

    if (allowedRoles.length && !allowedRoles.some(role => this.hasRole(role))) {
      saveFlash('error', 'You do not have access', "This page isn't available for your account.");
      window.location.assign(pageUrl('index.html'));
      return false;
    }

    return true;
  },

  consumeRedirectUrl(fallback = null) {
    const savedUrl = localStorage.getItem(STORAGE_KEYS.REDIRECT_URL);
    localStorage.removeItem(STORAGE_KEYS.REDIRECT_URL);
    if (!savedUrl) return fallback;

    try {
      const url = new URL(savedUrl, window.location.origin);
      if (url.origin !== window.location.origin
          || /\/pages\/(?:login|register)\.html$/.test(url.pathname)) {
        return fallback;
      }
      return url.href;
    } catch {
      return fallback;
    }
  }
};

export { ROLES };
export default auth;

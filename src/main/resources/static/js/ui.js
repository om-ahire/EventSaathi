/**
 * Shared presentation helpers. User content is always written as text, not
 * interpreted as markup.
 */
import { STORAGE_KEYS } from './config.js';

export function formatCurrency(amount) {
  const value = Number(amount);
  if (!Number.isFinite(value)) return '₹0';
  return new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency: 'INR',
    maximumFractionDigits: 0
  }).format(value);
}

export function formatDate(value) {
  if (!value) return '';

  const dateOnly = typeof value === 'string' && /^(\d{4})-(\d{2})-(\d{2})$/.exec(value);
  const date = dateOnly
    ? new Date(Number(dateOnly[1]), Number(dateOnly[2]) - 1, Number(dateOnly[3]))
    : value instanceof Date ? value : new Date(value);

  if (Number.isNaN(date.getTime())) return String(value);
  return new Intl.DateTimeFormat('en-IN', {
    day: 'numeric',
    month: 'short',
    year: 'numeric'
  }).format(date);
}

export function escapeHtml(value) {
  const entityMap = { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' };
  return String(value ?? '').replace(/[&<>"']/g, character => entityMap[character]);
}

export function debounce(callback, delay = 300) {
  let timer;
  return function debounced(...args) {
    clearTimeout(timer);
    timer = setTimeout(() => callback.apply(this, args), delay);
  };
}

const TOAST_ICONS = {
  success: 'bi-check-circle',
  error: 'bi-exclamation-circle',
  warning: 'bi-exclamation-triangle',
  info: 'bi-info-circle'
};

export function showToast(type = 'info', title = '', message = '') {
  const toastType = Object.hasOwn(TOAST_ICONS, type) ? type : 'info';
  let container = document.getElementById('es-toast-container');
  if (!container) {
    container = document.createElement('div');
    container.id = 'es-toast-container';
    container.className = 'es-toast-container';
    container.setAttribute('aria-live', 'polite');
    container.setAttribute('aria-atomic', 'false');
    document.body.append(container);
  }

  const toast = document.createElement('div');
  toast.className = `es-toast es-toast-${toastType}`;
  toast.setAttribute('role', toastType === 'error' ? 'alert' : 'status');

  const icon = document.createElement('i');
  icon.className = `bi ${TOAST_ICONS[toastType]} es-toast-icon`;
  icon.setAttribute('aria-hidden', 'true');

  const content = document.createElement('div');
  content.className = 'es-toast-content';

  if (title) {
    const heading = document.createElement('div');
    heading.className = 'es-toast-title';
    heading.textContent = title;
    content.append(heading);
  }

  const detail = document.createElement('div');
  detail.className = 'es-toast-message';
  detail.textContent = message;
  content.append(detail);

  const closeButton = document.createElement('button');
  closeButton.className = 'es-toast-close';
  closeButton.type = 'button';
  closeButton.setAttribute('aria-label', 'Dismiss notification');
  closeButton.innerHTML = '<i class="bi bi-x" aria-hidden="true"></i>';

  toast.append(icon, content, closeButton);
  container.append(toast);

  let dismissTimer;
  let removalTimer;
  function dismiss() {
    clearTimeout(dismissTimer);
    toast.classList.add('is-hiding');
    removalTimer = setTimeout(() => toast.remove(), 220);
  }
  closeButton.addEventListener('click', dismiss);
  toast.addEventListener('mouseenter', () => clearTimeout(dismissTimer));
  toast.addEventListener('mouseleave', () => {
    dismissTimer = setTimeout(dismiss, 4000);
  });
  dismissTimer = setTimeout(dismiss, 4000);

  return {
    dismiss,
    remove() {
      clearTimeout(dismissTimer);
      clearTimeout(removalTimer);
      toast.remove();
    }
  };
}

export function checkFlashMessage() {
  const flash = localStorage.getItem(STORAGE_KEYS.SESSION_FLASH);
  if (!flash) return;

  localStorage.removeItem(STORAGE_KEYS.SESSION_FLASH);
  try {
    const message = JSON.parse(flash);
    showToast(message.type, message.title, message.message);
  } catch {
    showToast('error', 'We could not display a notification', 'Please refresh the page if you continue to have trouble.');
  }
}

const originalButtonStates = new WeakMap();

export function showLoader(button, loadingText = 'Please wait') {
  if (!button || originalButtonStates.has(button)) return;
  originalButtonStates.set(button, {
    html: button.innerHTML,
    disabled: button.disabled,
    label: button.getAttribute('aria-label')
  });
  button.classList.add('is-loading');
  button.disabled = true;
  button.setAttribute('aria-label', loadingText);
}

export function hideLoader(button) {
  if (!button) return;
  const original = originalButtonStates.get(button);
  if (!original) return;

  button.innerHTML = original.html;
  button.disabled = original.disabled;
  button.classList.remove('is-loading');
  if (original.label === null) button.removeAttribute('aria-label');
  else button.setAttribute('aria-label', original.label);
  originalButtonStates.delete(button);
}

export function openModal(modalId) {
  const backdrop = document.getElementById(modalId);
  if (!backdrop) return;
  backdrop.classList.add('is-open');
  document.body.style.overflow = 'hidden';
  backdrop.querySelector('input:not([disabled]), textarea:not([disabled]), select:not([disabled]), button:not([disabled])')?.focus();
}

export function closeModal(modalId) {
  const backdrop = document.getElementById(modalId);
  if (!backdrop) return;
  backdrop.classList.remove('is-open');
  document.body.style.overflow = '';
}

function normalizeFieldErrors(fieldErrors) {
  if (!fieldErrors) return {};
  if (typeof fieldErrors === 'string') {
    return Object.fromEntries(
      fieldErrors.split(/;\s*/).flatMap(entry => {
        const separator = entry.indexOf(':');
        return separator > 0
          ? [[entry.slice(0, separator).trim(), entry.slice(separator + 1).trim()]]
          : [];
      })
    );
  }
  if (Array.isArray(fieldErrors)) {
    return Object.fromEntries(fieldErrors
      .filter(error => error?.field)
      .map(error => [error.field, error.defaultMessage || error.message || 'Check this value.']));
  }
  return typeof fieldErrors === 'object' ? fieldErrors : {};
}

export function showFormErrors(form, fieldErrors) {
  if (!form || !fieldErrors) return;
  clearFormErrors(form);

  for (const [field, message] of Object.entries(normalizeFieldErrors(fieldErrors))) {
    const input = [...form.elements].find(element => element.name === field || element.id === field);
    if (!input) continue;

    input.classList.add('is-invalid');
    input.setAttribute('aria-invalid', 'true');
    const group = input.closest('.es-form-group') || input.parentElement;
    let errorMessage = group.querySelector('.es-field-error');
    if (!errorMessage) {
      errorMessage = document.createElement('div');
      errorMessage.className = 'es-field-error';
      group.append(errorMessage);
    }
    errorMessage.id ||= `${input.id || input.name}-error`;
    errorMessage.setAttribute('role', 'alert');
    errorMessage.textContent = String(message);
    const existingDescription = input.getAttribute('aria-describedby');
    input.setAttribute('aria-describedby', [existingDescription, errorMessage.id].filter(Boolean).join(' '));
  }
}

export function clearFormErrors(form) {
  if (!form) return;
  for (const input of form.querySelectorAll('[aria-invalid="true"]')) {
    input.classList.remove('is-invalid');
    input.removeAttribute('aria-invalid');
    const errorId = input.getAttribute('aria-describedby');
    const group = input.closest('.es-form-group') || input.parentElement;
    const error = group.querySelector('.es-field-error');
    if (error && errorId) {
      input.setAttribute('aria-describedby', errorId.split(/\s+/).filter(id => id !== error.id).join(' '));
    }
  }
  form.querySelectorAll('.es-field-error[role="alert"]').forEach(error => error.remove());
}

if (typeof document !== 'undefined') {
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', checkFlashMessage, { once: true });
  } else {
    checkFlashMessage();
  }
}

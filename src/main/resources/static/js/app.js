import { mountLayout } from './components.js';

function start() {
  const page = window.location.pathname.split('/').pop()?.replace(/\.html$/, '') || 'home';
  mountLayout(document, page);
}

if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', start, { once: true });
} else {
  start();
}

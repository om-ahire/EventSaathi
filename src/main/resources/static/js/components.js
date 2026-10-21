/**
 * Shared, escaped markup and layout for every EventSaathi page.
 */
import { FALLBACK_VENDOR_IMAGE, ROLES } from './config.js';
import auth from './auth.js';
import { escapeHtml, formatCurrency, formatDate } from './ui.js';

const siteRoot = new URL('../', import.meta.url);
const pageHref = page => new URL(page, siteRoot).href;
const rootsWithImageFallback = new WeakSet();

const STATUS_CLASSES = {
  PENDING: 'pending',
  CONFIRMED: 'confirmed',
  ACCEPTED: 'accepted',
  REJECTED: 'rejected',
  COMPLETED: 'completed',
  CANCELLED: 'cancelled'
};

function navItem(label, page, activePage) {
  const active = page.replace('.html', '') === activePage;
  return `<li><a class="es-nav-link${active ? ' active' : ''}" href="${pageHref(`pages/${page}`)}"${active ? ' aria-current="page"' : ''}>${label}</a></li>`;
}

export function renderNavbar(activePage = '') {
  const user = auth.getCurrentUser();
  const roles = Array.isArray(user?.roles) ? user.roles : [];
  const isCustomer = roles.includes(ROLES.CUSTOMER);
  const isVendor = roles.includes(ROLES.VENDOR);
  const isAdmin = roles.includes(ROLES.ADMIN);

  const links = [
    navItem('Find vendors', 'vendors.html', activePage),
    navItem('Plan an event', 'plan-event.html', activePage)
  ];
  if (!user) {
    links.push(navItem('Log in', 'login.html', activePage));
    links.push(navItem('Create account', 'register.html', activePage));
  }
  if (isCustomer) {
    links.push(navItem('My events', 'my-events.html', activePage));
    links.push(navItem('My bookings', 'my-bookings.html', activePage));
  }
  if (isVendor) links.push(navItem('Vendor dashboard', 'vendor-dashboard.html', activePage));
  if (isAdmin) links.push(navItem('Admin', 'admin-dashboard.html', activePage));

  const accountActions = user
    ? `<span class="es-nav-user">${escapeHtml(user.name || user.email || 'Your account')}</span>
       <button class="es-btn es-btn-secondary es-btn-sm" type="button" data-es-logout>Sign out</button>`
    : '';

  return `
    <nav class="es-navbar" aria-label="Main navigation">
      <div class="es-container es-navbar-inner">
        <a class="es-brand" href="${pageHref('index.html')}" aria-label="EventSaathi home">
          EventSaathi<span class="es-brand-dot" aria-hidden="true"></span>
          <span class="es-brand-tagline">Plan a day worth remembering</span>
        </a>
        <ul class="es-nav-links" id="es-primary-navigation">${links.join('')}</ul>
        <div class="es-nav-actions">
          <div class="es-nav-account">${accountActions}</div>
          <button class="es-nav-toggle" type="button" aria-label="Open navigation menu"
                  aria-controls="es-primary-navigation" aria-expanded="false" data-es-nav-toggle>
            <i class="bi bi-list" aria-hidden="true"></i>
          </button>
        </div>
      </div>
    </nav>`;
}

export function renderFooter() {
  return `
    <footer class="es-footer motif-bg">
      <div class="es-container es-footer-inner">
        <a class="es-brand" href="${pageHref('index.html')}">
          EventSaathi<span class="es-brand-dot" aria-hidden="true"></span>
        </a>
        <p class="es-footer-copy">Good people make the day. We'll help you find them.</p>
        <a class="es-footer-link" href="${pageHref('pages/vendors.html')}">Explore vendors</a>
      </div>
    </footer>`;
}

function safeImageUrl(value) {
  if (typeof value !== 'string') return FALLBACK_VENDOR_IMAGE;
  try {
    const url = new URL(value, window.location.origin);
    return ['http:', 'https:'].includes(url.protocol) ? url.href : FALLBACK_VENDOR_IMAGE;
  } catch {
    return FALLBACK_VENDOR_IMAGE;
  }
}

export function renderVendorCard(vendor) {
  const vendorName = escapeHtml(vendor?.businessName || 'Event vendor');
  const vendorId = encodeURIComponent(String(vendor?.id ?? ''));
  const image = escapeHtml(safeImageUrl(vendor?.imageUrl));
  const category = escapeHtml(vendor?.category || 'Event service');
  const city = escapeHtml(vendor?.city || '');
  const rating = Number(vendor?.rating);
  const ratingMarkup = vendor?.rating != null && Number.isFinite(rating)
    ? `<span class="es-vendor-card-rating"><i class="bi bi-star-fill" aria-hidden="true"></i> ${rating.toFixed(1)}${Number.isFinite(Number(vendor?.totalReviews)) ? ` <span class="text-muted-warm">(${escapeHtml(vendor.totalReviews)})</span>` : ''}</span>`
    : '';

  return `
    <article class="es-card es-card-hover es-vendor-card">
      <a class="es-vendor-card-media" href="${pageHref(`pages/vendor-detail.html?id=${vendorId}`)}" aria-label="View ${vendorName}">
        <span class="es-badge es-badge-category es-vendor-card-badge-top-left">${category}</span>
        ${ratingMarkup}
        <img class="es-vendor-card-img" src="${image}" data-fallback-image="${escapeHtml(FALLBACK_VENDOR_IMAGE)}"
             alt="${vendorName}" loading="lazy">
      </a>
      <div class="es-card-body">
        <h3 class="es-vendor-card-title">${vendorName}</h3>
        ${city ? `<p class="es-vendor-card-meta"><i class="bi bi-geo-alt" aria-hidden="true"></i> ${city}</p>` : ''}
        <p class="es-vendor-description">${escapeHtml(vendor?.description || '')}</p>
        <div class="es-vendor-card-price">
          <div>
            <span class="es-vendor-card-price-label">Starting from</span>
            <div class="es-vendor-card-price-val">${vendor?.basePrice == null ? 'Price on request' : formatCurrency(vendor.basePrice)}</div>
          </div>
          <a class="es-btn es-btn-secondary es-btn-sm" href="${pageHref(`pages/vendor-detail.html?id=${vendorId}`)}">View details</a>
        </div>
      </div>
    </article>`;
}

export function renderBookingRow(booking) {
  const status = String(booking?.status || 'PENDING').toUpperCase();
  const statusClass = STATUS_CLASSES[status] || 'pending';
  const eventName = escapeHtml(booking?.eventName || 'Event');
  const vendorName = escapeHtml(booking?.vendorName || 'Vendor');
  const date = escapeHtml(formatDate(booking?.bookingDate));
  const amount = escapeHtml(formatCurrency(booking?.totalAmount));

  return `
    <tr>
      <td>${eventName}</td>
      <td>${vendorName}</td>
      <td>${date}</td>
      <td><span class="es-badge es-badge-${statusClass}">${escapeHtml(status[0] + status.slice(1).toLowerCase())}</span></td>
      <td class="inr-currency">${amount}</td>
    </tr>`;
}

export function mountLayout(root = document, activePage = '') {
  root.querySelectorAll('[data-es-navbar]').forEach(slot => {
    slot.innerHTML = renderNavbar(activePage);
    const toggle = slot.querySelector('[data-es-nav-toggle]');
    const links = slot.querySelector('.es-nav-links');
    toggle?.addEventListener('click', () => {
      const expanded = toggle.getAttribute('aria-expanded') === 'true';
      toggle.setAttribute('aria-expanded', String(!expanded));
      toggle.setAttribute('aria-label', expanded ? 'Open navigation menu' : 'Close navigation menu');
      toggle.querySelector('i').className = `bi ${expanded ? 'bi-list' : 'bi-x-lg'}`;
      links?.classList.toggle('is-open', !expanded);
    });
    slot.querySelector('[data-es-logout]')?.addEventListener('click', () => auth.logout());
    links?.addEventListener('click', event => {
      if (event.target.closest('a')) {
        links.classList.remove('is-open');
        toggle?.setAttribute('aria-expanded', 'false');
        toggle?.setAttribute('aria-label', 'Open navigation menu');
      }
    });
  });

  root.querySelectorAll('[data-es-footer]').forEach(slot => {
    slot.innerHTML = renderFooter();
  });

  if (!rootsWithImageFallback.has(root)) {
    root.addEventListener('error', event => {
      const image = event.target.closest?.('img[data-fallback-image]');
      if (!image || image.dataset.fallbackApplied) return;
      image.dataset.fallbackApplied = 'true';
      image.src = image.dataset.fallbackImage;
    }, true);
    rootsWithImageFallback.add(root);
  }
}

import { api } from '../api.js';
import { FALLBACK_VENDOR_IMAGE, ROLES } from '../config.js';
import auth from '../auth.js';
import { escapeHtml, formatCurrency, formatDate, hideLoader, showLoader, showToast } from '../ui.js';

const params = new URLSearchParams(window.location.search);
const vendorId = params.get('id');
const content = document.getElementById('vendor-detail-content');
const dialog = document.getElementById('vendor-booking-dialog');
const bookingForm = document.getElementById('vendor-booking-form');
const bookingEvent = document.getElementById('booking-event');
const bookingDate = document.getElementById('vendor-date-picker');
const packageSelect = document.getElementById('booking-package');
let vendor = null;
let services = [];
let events = [];
let cities = [];
let selectedReviews = [];
let pendingEvent = null;

function localDateValue(date = new Date()) {
  const local = new Date(date.getTime() - date.getTimezoneOffset() * 60000);
  return local.toISOString().slice(0, 10);
}

function fillOptions(select, items, placeholder) {
  select.replaceChildren(new Option(placeholder, ''));
  for (const item of items) {
    select.add(new Option(item.name, String(item.id)));
  }
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

function whatsappUrl(phone) {
  const digits = String(phone || '').replace(/\D/g, '');
  if (!digits) return '';
  const indianNumber = digits.length === 10 ? `91${digits}` : digits;
  return `https://wa.me/${indianNumber}`;
}

function renderRating(rating, reviewCount) {
  const value = Number(rating);
  if (!Number.isFinite(value) || reviewCount == null || Number(reviewCount) === 0) {
    return '<span class="vendor-rating-new"><i class="bi bi-stars" aria-hidden="true"></i> New to EventSaathi</span>';
  }
  return `<span class="vendor-rating-score"><i class="bi bi-star-fill" aria-hidden="true"></i> ${escapeHtml(value.toFixed(1))}</span>
    <span class="vendor-rating-count">${Number(reviewCount).toLocaleString('en-IN')} ${Number(reviewCount) === 1 ? 'review' : 'reviews'}</span>`;
}

function renderPackages() {
  const container = document.getElementById('vendor-package-list');
  if (!services.length) {
    container.innerHTML = `<article class="vendor-package-card vendor-package-custom">
      <div><h3>Talk through what you need</h3><p>Send a request with your event details and ask the vendor for a quote.</p></div>
      <strong>${vendor.basePrice == null ? 'Price on request' : `From ${formatCurrency(vendor.basePrice)}`}</strong>
    </article>`;
    packageSelect.replaceChildren(new Option('Custom enquiry', ''));
    document.getElementById('booking-package-price').textContent = vendor.basePrice == null
      ? 'The vendor can share a quote after learning more.'
      : `Starting from ${formatCurrency(vendor.basePrice)}.`;
    return;
  }

  container.innerHTML = services.map(service => `
    <article class="vendor-package-card">
      <div class="vendor-package-copy">
        <h3>${escapeHtml(service.name)}</h3>
        <p>${escapeHtml(service.description || 'Ask the vendor for details about this service.')}</p>
        <ul class="vendor-package-facts">
          ${service.maxGuests ? `<li><i class="bi bi-people" aria-hidden="true"></i> Up to ${Number(service.maxGuests).toLocaleString('en-IN')} guests</li>` : ''}
          ${service.durationHours ? `<li><i class="bi bi-clock" aria-hidden="true"></i> ${Number(service.durationHours)} hours</li>` : ''}
        </ul>
      </div>
      <strong class="vendor-package-price">${formatCurrency(service.price)}</strong>
    </article>`).join('');
  packageSelect.replaceChildren(
    new Option('Choose a service', ''),
    ...services.map(service => new Option(`${service.name} — ${formatCurrency(service.price)}`, String(service.id)))
  );
  document.getElementById('booking-package-price').textContent = 'Choose a service to include its price in your request.';
}

function renderReviews() {
  const container = document.getElementById('vendor-review-list');
  const count = Number(vendor.totalReviews) || 0;
  document.getElementById('vendor-review-summary').textContent = count
    ? `${Number(vendor.rating).toFixed(1)} average from ${count.toLocaleString('en-IN')} reviews`
    : '';

  if (!selectedReviews.length) {
    container.innerHTML = '<p class="vendor-reviews-empty">No written reviews just yet. Check back as more customers share their experience.</p>';
    return;
  }

  container.innerHTML = selectedReviews.map(review => {
    const rating = Math.min(5, Math.max(0, Number(review.rating) || 0));
    const date = formatDate(review.createdAt);
    return `<article class="vendor-review-card">
      <div class="vendor-review-topline">
        <strong>${escapeHtml(review.customerName || 'EventSaathi customer')}</strong>
        <time${date ? ` datetime="${escapeHtml(review.createdAt)}"` : ''}>${escapeHtml(date)}</time>
      </div>
      <div class="vendor-review-stars" aria-label="${rating} out of 5 stars">${'<i class="bi bi-star-fill" aria-hidden="true"></i>'.repeat(rating)}</div>
      <p>${escapeHtml(review.comment || 'A rating shared after the event.')}</p>
    </article>`;
  }).join('');
}

function renderVendor(data) {
  vendor = data;
  services = Array.isArray(data.services) ? data.services : [];
  document.title = `${data.businessName || 'Vendor details'} | EventSaathi`;
  document.getElementById('vendor-breadcrumb-name').textContent = data.businessName || 'Vendor details';
  document.getElementById('vendor-name').textContent = data.businessName || 'Event vendor';
  document.getElementById('vendor-category').textContent = data.category || 'Event service';
  document.getElementById('vendor-location').innerHTML = data.city
    ? `<i class="bi bi-geo-alt" aria-hidden="true"></i> ${escapeHtml(data.city)}${data.address ? ` <span aria-hidden="true">·</span> ${escapeHtml(data.address)}` : ''}`
    : '';
  document.getElementById('vendor-rating').innerHTML = renderRating(data.rating, data.totalReviews);
  document.getElementById('vendor-short-description').textContent = data.description || 'A local event professional, ready to hear about your plans.';
  document.getElementById('vendor-description').textContent = data.description || 'Get in touch with this vendor to learn more about their services and approach.';
  document.getElementById('vendor-image').src = safeImageUrl(data.imageUrl);
  document.getElementById('vendor-image').alt = data.businessName ? `Work by ${data.businessName}` : 'Event vendor work';
  document.getElementById('vendor-starting-price').textContent = data.basePrice == null
    ? 'Price on request'
    : formatCurrency(data.basePrice);
  document.getElementById('vendor-mobile-price').textContent = data.basePrice == null
    ? 'Price on request'
    : formatCurrency(data.basePrice);

  const address = document.getElementById('vendor-address');
  address.hidden = !data.address;
  address.querySelector('span').textContent = data.address || '';

  const whatsapp = document.getElementById('vendor-whatsapp');
  const chatUrl = whatsappUrl(data.contactPhone);
  whatsapp.hidden = !chatUrl;
  if (chatUrl) whatsapp.href = chatUrl;

  const contact = document.getElementById('vendor-contact-link');
  contact.hidden = !data.contactEmail;
  if (data.contactEmail) {
    contact.href = `mailto:${encodeURIComponent(data.contactEmail)}`;
    contact.textContent = data.contactEmail;
  }

  document.getElementById('booking-vendor-name').textContent = data.businessName || 'Event vendor';
  document.getElementById('booking-event-name').value = `${data.category || 'Event'} with ${data.businessName || 'vendor'}`;
  const citySelect = document.getElementById('booking-city');
  if (data.cityId && [...citySelect.options].some(option => option.value === String(data.cityId))) {
    citySelect.value = String(data.cityId);
  }
  renderPackages();
  document.getElementById('vendor-detail-loading').hidden = true;
  content.hidden = false;
  document.getElementById('vendor-mobile-booking-bar').hidden = false;
}

function displayAvailableEvents(list) {
  events = list.filter(item => !['CANCELLED', 'COMPLETED'].includes(String(item.status).toUpperCase()));
  bookingEvent.replaceChildren(new Option('Create a new event', 'new'));
  for (const event of events) {
    const date = formatDate(event.eventDate);
    bookingEvent.add(new Option(`${event.name} · ${date}`, String(event.id)));
  }
}

function toggleEventFields() {
  const isNew = bookingEvent.value === 'new';
  document.getElementById('booking-new-event-fields').hidden = !isNew;
  const existing = events.find(item => String(item.id) === bookingEvent.value);
  const hint = document.getElementById('booking-event-date-hint');
  if (existing) {
    bookingDate.value = existing.eventDate || '';
    bookingDate.disabled = true;
    hint.textContent = `This event is on ${formatDate(existing.eventDate)}.`;
  } else {
    bookingDate.disabled = false;
    hint.textContent = '';
  }
  document.getElementById('booking-selected-date').textContent = bookingDate.value
    ? formatDate(bookingDate.value)
    : 'Choose a date';
}

function clearBookingErrors() {
  bookingForm.querySelectorAll('.es-field-error').forEach(element => {
    element.textContent = '';
    element.hidden = true;
  });
  bookingForm.querySelectorAll('[aria-invalid="true"]').forEach(element => {
    element.removeAttribute('aria-invalid');
    element.classList.remove('is-invalid');
  });
  const error = document.getElementById('booking-form-error');
  error.hidden = true;
  error.textContent = '';
}

function displayBookingErrors(fieldErrors) {
  if (!fieldErrors) return;
  for (const [name, message] of Object.entries(fieldErrors)) {
    const field = bookingForm.elements.namedItem(name);
    const error = bookingForm.querySelector(`[data-error-for="${CSS.escape(name)}"]`);
    if (field) {
      field.setAttribute('aria-invalid', 'true');
      field.classList.add('is-invalid');
    }
    if (error) {
      error.textContent = String(message);
      error.hidden = false;
    }
  }
}

function eventRequestBody() {
  const formData = new FormData(bookingForm);
  const guestCount = String(formData.get('guestCount') || '').trim();
  const totalBudget = String(formData.get('totalBudget') || '').trim();
  return {
    name: String(formData.get('name') || '').trim(),
    eventDate: bookingDate.value,
    eventTypeId: formData.get('eventTypeId') ? Number(formData.get('eventTypeId')) : null,
    cityId: formData.get('cityId') ? Number(formData.get('cityId')) : null,
    guestCount: guestCount ? Number(guestCount) : null,
    totalBudget: totalBudget ? Number(totalBudget) : null,
    description: String(formData.get('description') || '').trim()
  };
}

async function resolveEventId() {
  if (bookingEvent.value !== 'new') return Number(bookingEvent.value);
  const body = eventRequestBody();
  if (pendingEvent) {
    const updatedEvent = await api.put(`/events/${encodeURIComponent(pendingEvent.id)}`, body);
    pendingEvent = updatedEvent;
  } else {
    pendingEvent = await api.post('/events', body);
  }
  return Number(pendingEvent.id);
}

async function submitBooking(event) {
  event.preventDefault();
  clearBookingErrors();
  if (!bookingForm.reportValidity()) return;
  if (!bookingDate.value) {
    bookingDate.setAttribute('aria-invalid', 'true');
    document.getElementById('booking-form-error').textContent = 'Choose an event date to continue.';
    document.getElementById('booking-form-error').hidden = false;
    bookingDate.focus();
    return;
  }
  if (services.length && !packageSelect.value) {
    packageSelect.setAttribute('aria-invalid', 'true');
    document.getElementById('booking-form-error').textContent = 'Choose a service package to continue.';
    document.getElementById('booking-form-error').hidden = false;
    packageSelect.focus();
    return;
  }

  const submitButton = document.getElementById('booking-submit');
  showLoader(submitButton, 'Sending your request');
  try {
    const eventId = await resolveEventId();
    const vendorServiceId = packageSelect.value ? Number(packageSelect.value) : null;
    await api.post('/bookings', {
      eventId,
      vendorId: Number(vendor.id),
      vendorServiceId,
      notes: String(new FormData(bookingForm).get('notes') || '').trim()
    });
    pendingEvent = null;
    document.getElementById('booking-form-error').hidden = true;
    bookingForm.hidden = true;
    document.getElementById('booking-success').hidden = false;
    showToast('success', 'Request sent', 'Your vendor request has been submitted.');
  } catch (error) {
    if (error.fieldErrors) displayBookingErrors(error.fieldErrors);
    const message = pendingEvent
      ? `Your event has been saved. ${error.message} You can try the request again without creating another event.`
      : error.message || 'Please check your details and try again.';
    document.getElementById('booking-form-error').textContent = message;
    document.getElementById('booking-form-error').hidden = false;
    showToast('error', 'We could not send the request', error.message || 'Please check your details and try again.');
  } finally {
    hideLoader(submitButton);
  }
}

async function prepareBookingOptions() {
  try {
    const eventTypeResponse = await api.get('/event-types');
    fillOptions(document.getElementById('booking-event-type'),
      Array.isArray(eventTypeResponse) ? eventTypeResponse : [], 'Choose an occasion');
  } catch (error) {
    showToast('error', 'Occasions unavailable', error.message || 'We could not load event types.');
  }

  try {
    const cityResponse = await api.get('/cities');
    cities = Array.isArray(cityResponse) ? cityResponse : [];
    fillOptions(document.getElementById('booking-city'), cities, 'Choose a city');
    if (vendor?.cityId) document.getElementById('booking-city').value = String(vendor.cityId);
  } catch (error) {
    showToast('error', 'Event details unavailable', error.message || 'We could not load city options.');
  }

  if (auth.hasRole(ROLES.CUSTOMER)) {
    try {
      const eventResponse = await api.get('/events');
      displayAvailableEvents(Array.isArray(eventResponse) ? eventResponse : []);
    } catch (error) {
      showToast('error', 'Your saved events are unavailable', error.message || 'You can still create a new event.');
    }
  } else {
    displayAvailableEvents([]);
  }
}

async function openBooking() {
  if (!auth.isAuthenticated() || !auth.hasRole(ROLES.CUSTOMER)) {
    const returnUrl = new URL(window.location.href);
    returnUrl.searchParams.set('book', '1');
    window.history.replaceState(null, '', returnUrl);
    auth.requireAuth([ROLES.CUSTOMER]);
    return;
  }

  clearBookingErrors();
  document.getElementById('booking-success').hidden = true;
  bookingForm.hidden = false;
  if (!bookingDate.value) bookingDate.value = localDateValue();
  await prepareBookingOptions();
  toggleEventFields();
  dialog.showModal();
}

async function init() {
  bookingDate.min = localDateValue();
  bookingDate.value = params.get('date') || localDateValue();
  if (!vendorId || !/^\d+$/.test(vendorId)) {
    document.getElementById('vendor-detail-loading').hidden = true;
    document.getElementById('vendor-detail-error').hidden = false;
    return;
  }

  try {
    const response = await api.get(`/vendors/${encodeURIComponent(vendorId)}`);
    if (!response || !response.id) throw new Error('This vendor profile is not available.');
    renderVendor(response);
    try {
      const reviewResponse = await api.get(`/vendors/${encodeURIComponent(vendorId)}/reviews`);
      selectedReviews = Array.isArray(reviewResponse) ? reviewResponse : [];
      renderReviews();
    } catch {
      document.getElementById('vendor-review-list').innerHTML = '<p class="vendor-reviews-empty">Customer reviews are unavailable right now. Please try again later.</p>';
    }
  } catch (error) {
    document.getElementById('vendor-detail-loading').hidden = true;
    document.getElementById('vendor-detail-error').hidden = false;
    showToast('error', 'Vendor profile unavailable', error.message || 'Please try opening this profile again.');
    return;
  }

  bookingDate.addEventListener('change', () => {
    document.getElementById('booking-selected-date').textContent = bookingDate.value
      ? formatDate(bookingDate.value)
      : 'Choose a date';
  });
  bookingEvent.addEventListener('change', toggleEventFields);
  packageSelect.addEventListener('change', () => {
    const service = services.find(item => String(item.id) === packageSelect.value);
    document.getElementById('booking-package-price').textContent = service
      ? `Requesting ${service.name} at ${formatCurrency(service.price)}.`
      : 'Choose a service to include its price in your request.';
  });
  bookingForm.addEventListener('submit', submitBooking);
  document.querySelectorAll('[data-vendor-book-trigger]').forEach(button => button.addEventListener('click', openBooking));
  document.getElementById('vendor-book-now').addEventListener('click', openBooking);
  document.querySelectorAll('[data-dialog-close]').forEach(button => button.addEventListener('click', () => dialog.close()));
  dialog.addEventListener('click', event => {
    if (event.target === dialog) dialog.close();
  });

  const typeSelect = document.getElementById('booking-event-type');
  const requestedType = params.get('eventTypeId');
  if (requestedType && [...typeSelect.options].some(option => option.value === requestedType)) {
    typeSelect.value = requestedType;
  }

  if (params.get('book') === '1' && auth.hasRole(ROLES.CUSTOMER)) {
    const nextUrl = new URL(window.location.href);
    nextUrl.searchParams.delete('book');
    window.history.replaceState(null, '', nextUrl);
    await openBooking();
  }
}

init();

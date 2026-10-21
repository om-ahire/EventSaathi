import { api } from '../api.js';
import { renderVendorCard } from '../components.js';
import { debounce, escapeHtml, showToast } from '../ui.js';

const FILTER_KEYS = ['q', 'categoryId', 'category', 'cityId', 'city', 'maxPrice', 'minRating', 'page', 'sort', 'direction'];
const PAGE_SIZE = 12;
const form = document.getElementById('vendors-filter-form');
const grid = document.getElementById('vendors-grid');
const filterDrawer = document.getElementById('vendors-filters');
const filterScrim = document.getElementById('vendors-filter-scrim');
let categories = [];
let cities = [];
let currentPage = Math.max(0, Number.parseInt(new URLSearchParams(window.location.search).get('page') || '0', 10) || 0);
let activeRequest = 0;

function selectedSort() {
  const [sort, direction] = document.getElementById('vendors-sort').value.split(':');
  return { sort, direction };
}

function makeSearchParams(page = currentPage) {
  const search = new URLSearchParams(window.location.search);
  for (const key of FILTER_KEYS) search.delete(key);

  const values = new FormData(form);
  for (const key of ['q', 'categoryId', 'cityId', 'maxPrice', 'minRating']) {
    const value = String(values.get(key) || '').trim();
    if (value) search.set(key, value);
  }

  const category = categories.find(item => String(item.id) === String(values.get('categoryId')));
  const city = cities.find(item => String(item.id) === String(values.get('cityId')));
  if (category) search.set('category', category.name);
  if (city) search.set('city', city.name);

  const sort = selectedSort();
  search.set('sort', sort.sort);
  search.set('direction', sort.direction);
  if (page > 0) search.set('page', String(page));
  return search;
}

function writeUrl(search) {
  const query = search.toString();
  window.history.replaceState(null, '', `${window.location.pathname}${query ? `?${query}` : ''}`);
}

function updateActiveFilters(search) {
  const chipNames = { q: 'Search', categoryId: 'Category', cityId: 'City', maxPrice: 'Up to', minRating: 'Rated' };
  const chips = [];
  for (const [key, label] of Object.entries(chipNames)) {
    const value = search.get(key);
    if (!value) continue;

    let display = value;
    if (key === 'categoryId') display = search.get('category') || categories.find(item => String(item.id) === value)?.name || label;
    if (key === 'cityId') display = search.get('city') || cities.find(item => String(item.id) === value)?.name || label;
    if (key === 'maxPrice') display = `Up to ₹${Number(value).toLocaleString('en-IN')}`;
    if (key === 'minRating') display = `${value}+ stars`;
    if (key === 'q') display = `“${value}”`;

    chips.push(`<button class="es-filter-chip active" type="button" data-remove-filter="${key}" aria-label="Remove ${escapeHtml(label)} filter">${escapeHtml(display)} <i class="bi bi-x es-filter-chip-remove" aria-hidden="true"></i></button>`);
  }

  const container = document.getElementById('vendors-active-filters');
  container.innerHTML = chips.join('');
  container.hidden = chips.length === 0;
  const badge = document.getElementById('vendors-filter-count');
  badge.hidden = chips.length === 0;
  badge.textContent = String(chips.length);
  container.querySelectorAll('[data-remove-filter]').forEach(button => {
    button.addEventListener('click', () => {
      const key = button.dataset.removeFilter;
      const input = form.elements.namedItem(key);
      if (input) input.value = '';
      currentPage = 0;
      loadVendors();
    });
  });
}

function skeletonMarkup() {
  return Array.from({ length: 6 }, () => `
    <div class="col-md-6 col-xl-4">
      <div class="es-card es-vendor-card" aria-hidden="true">
        <div class="es-skeleton es-skeleton-media"></div>
        <div class="es-card-body">
          <div class="es-skeleton es-skeleton-title"></div>
          <div class="es-skeleton es-skeleton-text"></div>
          <div class="es-skeleton es-skeleton-text"></div>
          <div class="es-skeleton es-skeleton-btn"></div>
        </div>
      </div>
    </div>`).join('');
}

function renderPagination(totalPages, page) {
  const container = document.getElementById('vendors-pagination');
  if (totalPages <= 1) {
    container.innerHTML = '';
    return;
  }

  const start = Math.max(0, Math.min(page - 2, totalPages - 5));
  const end = Math.min(totalPages, start + 5);
  const items = [];
  items.push(`<button class="es-page-item" type="button" data-page="${page - 1}" aria-label="Previous page"${page === 0 ? ' disabled' : ''}><i class="bi bi-chevron-left" aria-hidden="true"></i></button>`);
  for (let number = start; number < end; number += 1) {
    items.push(`<button class="es-page-item${number === page ? ' active' : ''}" type="button" data-page="${number}" aria-label="Page ${number + 1}"${number === page ? ' aria-current="page"' : ''}>${number + 1}</button>`);
  }
  items.push(`<button class="es-page-item" type="button" data-page="${page + 1}" aria-label="Next page"${page >= totalPages - 1 ? ' disabled' : ''}><i class="bi bi-chevron-right" aria-hidden="true"></i></button>`);
  container.innerHTML = items.join('');
  container.querySelectorAll('button:not(:disabled)').forEach(button => {
    button.addEventListener('click', () => {
      currentPage = Number(button.dataset.page);
      loadVendors();
      window.scrollTo({ top: 0, behavior: 'smooth' });
    });
  });
}

function updateContext() {
  const params = new URLSearchParams(window.location.search);
  const eventName = params.get('event');
  const eventContext = document.getElementById('vendors-event-context');
  if (eventName) {
    eventContext.querySelector('span').textContent = `Planning a ${eventName}?`;
    eventContext.hidden = false;
  } else {
    eventContext.hidden = true;
  }

  const city = params.get('city');
  document.querySelector('.vendors-header h1').textContent = city
    ? `Event vendors in ${city}.`
    : 'Find the people who make it happen.';
}

function fillSelect(select, entries, placeholder) {
  select.replaceChildren(new Option(placeholder, ''));
  for (const entry of entries) select.add(new Option(entry.name, String(entry.id)));
}

function applyInitialFilters() {
  const params = new URLSearchParams(window.location.search);
  for (const key of ['q', 'categoryId', 'cityId', 'maxPrice', 'minRating']) {
    const field = form.elements.namedItem(key);
    if (field) field.value = params.get(key) || '';
  }

  const sort = params.get('sort') || 'rating';
  const direction = params.get('direction') || (sort === 'price' ? 'asc' : 'desc');
  const value = `${sort}:${direction}`;
  document.getElementById('vendors-sort').value = value;
  document.getElementById('vendors-sort-mobile').value = value;
}

async function loadVendors() {
  const requestId = ++activeRequest;
  const search = makeSearchParams();
  writeUrl(search);
  updateActiveFilters(search);
  grid.setAttribute('aria-busy', 'true');
  grid.innerHTML = skeletonMarkup();
  document.getElementById('vendors-empty').hidden = true;
  document.getElementById('vendors-error').hidden = true;
  document.getElementById('vendors-pagination').innerHTML = '';

  const apiParams = new URLSearchParams();
  for (const key of ['categoryId', 'cityId', 'maxPrice', 'minRating', 'q', 'page', 'sort', 'direction']) {
    if (search.has(key)) apiParams.set(key, search.get(key));
  }
  apiParams.set('page', String(currentPage));
  apiParams.set('size', String(PAGE_SIZE));

  try {
    const result = await api.get(`/vendors?${apiParams}`);
    if (requestId !== activeRequest) return;
    const vendors = Array.isArray(result?.content) ? result.content : [];
    grid.innerHTML = vendors.map(vendor => `
      <div class="col-md-6 col-xl-4">${renderVendorCard(vendor)}</div>`).join('');

    const total = Number(result?.totalElements) || vendors.length;
    const totalPages = Number(result?.totalPages) || (total ? 1 : 0);
    const page = Number(result?.number) || 0;
    const start = total ? page * PAGE_SIZE + 1 : 0;
    const end = Math.min((page + 1) * PAGE_SIZE, total);
    document.getElementById('vendors-result-count').textContent = total
      ? `${start.toLocaleString('en-IN')}–${end.toLocaleString('en-IN')} of ${total.toLocaleString('en-IN')} vendors`
      : 'No vendors to show just yet';
    document.getElementById('vendors-empty').hidden = vendors.length > 0;
    renderPagination(totalPages, page);
  } catch (error) {
    if (requestId !== activeRequest) return;
    grid.innerHTML = '';
    document.getElementById('vendors-result-count').textContent = 'Your shortlist is waiting.';
    document.getElementById('vendors-error').hidden = false;
    showToast('error', 'Vendor list unavailable', error.message || 'Please check your connection and try again.');
  } finally {
    if (requestId === activeRequest) grid.setAttribute('aria-busy', 'false');
  }
}

function closeDrawer() {
  filterDrawer.classList.remove('is-open');
  filterScrim.hidden = true;
  document.body.classList.remove('vendors-filters-open');
  document.getElementById('vendors-open-filters').setAttribute('aria-expanded', 'false');
  filterDrawer.setAttribute('aria-hidden', 'true');
}

function openDrawer() {
  filterScrim.hidden = false;
  requestAnimationFrame(() => filterDrawer.classList.add('is-open'));
  document.body.classList.add('vendors-filters-open');
  document.getElementById('vendors-open-filters').setAttribute('aria-expanded', 'true');
  filterDrawer.setAttribute('aria-hidden', 'false');
  document.getElementById('vendors-close-filters').focus();
}

function bindControls() {
  document.getElementById('filter-query').addEventListener('input', debounce(() => {
    currentPage = 0;
    loadVendors();
  }, 350));

  for (const key of ['categoryId', 'cityId', 'maxPrice', 'minRating']) {
    form.elements.namedItem(key).addEventListener('change', () => {
      currentPage = 0;
      loadVendors();
    });
  }

  form.addEventListener('reset', () => {
    requestAnimationFrame(() => {
      currentPage = 0;
      loadVendors();
    });
  });

  const handleSort = event => {
    const value = event.target.value;
    document.getElementById('vendors-sort').value = value;
    document.getElementById('vendors-sort-mobile').value = value;
    currentPage = 0;
    loadVendors();
  };
  document.getElementById('vendors-sort').addEventListener('change', handleSort);
  document.getElementById('vendors-sort-mobile').addEventListener('change', handleSort);
  document.getElementById('vendors-open-filters').addEventListener('click', openDrawer);
  document.getElementById('vendors-close-filters').addEventListener('click', closeDrawer);
  filterScrim.addEventListener('click', closeDrawer);
  document.getElementById('vendors-apply-filters').addEventListener('click', closeDrawer);
  document.getElementById('vendors-empty-reset').addEventListener('click', () => form.reset());
  document.getElementById('vendors-retry').addEventListener('click', loadVendors);
  document.addEventListener('keydown', event => {
    if (event.key === 'Escape' && filterDrawer.classList.contains('is-open')) {
      closeDrawer();
      document.getElementById('vendors-open-filters').focus();
    }
  });
  window.addEventListener('popstate', () => {
    currentPage = Math.max(0, Number.parseInt(new URLSearchParams(window.location.search).get('page') || '0', 10) || 0);
    applyInitialFilters();
    updateContext();
    loadVendors();
  });
}

async function init() {
  try {
    const [categoryResponse, cityResponse] = await Promise.all([
      api.get('/categories'),
      api.get('/cities')
    ]);
    categories = Array.isArray(categoryResponse) ? categoryResponse : [];
    cities = Array.isArray(cityResponse) ? cityResponse : [];
    fillSelect(document.getElementById('filter-category'), categories, 'All categories');
    fillSelect(document.getElementById('filter-city'), cities, 'All cities');
    applyInitialFilters();
    updateContext();
    bindControls();
    loadVendors();
  } catch (error) {
    document.getElementById('vendors-result-count').textContent = 'We could not load the filters.';
    document.getElementById('vendors-error').hidden = false;
    showToast('error', 'Marketplace filters unavailable', error.message || 'Please refresh to try again.');
  }
}

init();

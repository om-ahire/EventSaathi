import { api } from '../api.js';
import { FALLBACK_VENDOR_IMAGE } from '../config.js';
import { renderVendorCard } from '../components.js';
import { escapeHtml, showToast } from '../ui.js';

const CATEGORY_PHOTOS = [
  'photo-1519167758481-83f550bb49b3',
  'photo-1555244162-803834f70033',
  'photo-1519741497674-611481863552',
  'photo-1537633552985-df8429e8048b',
  'photo-1492684223066-81342ee5ff30',
  'photo-1511795409834-ef04bbd61622'
];

function photoForCategory(index) {
  const photoId = CATEGORY_PHOTOS[index % CATEGORY_PHOTOS.length];
  return `https://images.unsplash.com/${photoId}?auto=format&fit=crop&w=560&q=78`;
}

function renderCategories(categories) {
  const container = document.getElementById('home-categories');
  if (!categories.length) {
    container.innerHTML = '';
    const message = document.getElementById('home-categories-message');
    message.textContent = 'Browse all vendors to see what is available in your city.';
    message.hidden = false;
    return;
  }

  container.innerHTML = categories.slice(0, 6).map((category, index) => {
    const name = escapeHtml(category.name);
    const id = encodeURIComponent(String(category.id));
    return `
      <a class="home-category-tile" href="pages/vendors.html?categoryId=${id}&amp;category=${encodeURIComponent(category.name)}">
        <span class="home-category-photo">
          <img src="${photoForCategory(index)}" data-fallback-image="${FALLBACK_VENDOR_IMAGE}" alt="" loading="lazy">
          <span class="home-category-photo-shade" aria-hidden="true"></span>
          <span class="home-category-arrow" aria-hidden="true"><i class="bi bi-arrow-up-right"></i></span>
        </span>
        <span class="home-category-name">${name}</span>
      </a>`;
  }).join('');
}

function fillSelect(select, entries) {
  const placeholder = select.options[0];
  select.replaceChildren(placeholder);
  for (const entry of entries) {
    const option = document.createElement('option');
    option.value = String(entry.id);
    option.textContent = entry.name;
    select.append(option);
  }
}

function prepareSearch(cities, eventTypes) {
  const form = document.getElementById('home-search-form');
  const citySelect = document.getElementById('home-city');
  const eventSelect = document.getElementById('home-event');
  fillSelect(citySelect, cities);
  fillSelect(eventSelect, eventTypes);

  const params = new URLSearchParams(window.location.search);
  const requestedCity = params.get('cityId');
  const requestedEvent = params.get('eventTypeId');
  if (requestedCity && [...citySelect.options].some(option => option.value === requestedCity)) {
    citySelect.value = requestedCity;
  }
  if (requestedEvent && [...eventSelect.options].some(option => option.value === requestedEvent)) {
    eventSelect.value = requestedEvent;
  }

  form.addEventListener('submit', event => {
    event.preventDefault();
    const search = new URLSearchParams();
    const cityId = citySelect.value;
    const eventTypeId = eventSelect.value;
    const maxPrice = form.elements.maxPrice.value.trim();

    if (cityId) {
      search.set('cityId', cityId);
      search.set('city', citySelect.selectedOptions[0].textContent);
    }
    if (eventTypeId) {
      search.set('eventTypeId', eventTypeId);
      search.set('event', eventSelect.selectedOptions[0].textContent);
    }
    if (maxPrice && Number.isFinite(Number(maxPrice)) && Number(maxPrice) >= 0) {
      search.set('maxPrice', String(Number(maxPrice)));
    }

    const destination = new URL('pages/vendors.html', window.location.href);
    destination.search = search.toString();
    window.location.assign(destination.href);
  });
}

async function loadHome() {
  const featuredGrid = document.getElementById('home-featured-vendors');
  try {
    const [cities, categories, eventTypes, page] = await Promise.all([
      api.get('/cities'),
      api.get('/categories'),
      api.get('/event-types'),
      api.get('/vendors?page=0&size=3&sort=rating&direction=desc')
    ]);

    const cityList = Array.isArray(cities) ? cities : [];
    const categoryList = Array.isArray(categories) ? categories : [];
    const eventList = Array.isArray(eventTypes) ? eventTypes : [];
    prepareSearch(cityList, eventList);
    renderCategories(categoryList);

    const vendors = Array.isArray(page?.content) ? page.content : [];
    featuredGrid.innerHTML = vendors.map(vendor => `
      <div class="col-md-6 col-lg-4">${renderVendorCard(vendor)}</div>`).join('');
    if (!vendors.length) {
      featuredGrid.innerHTML = '';
      const message = document.getElementById('home-featured-message');
      message.textContent = 'There are no featured vendors to show just yet. Browse the marketplace to explore your options.';
      message.hidden = false;
    }
  } catch {
    featuredGrid.innerHTML = '';
    document.getElementById('home-categories').innerHTML = '';
    const categoryMessage = document.getElementById('home-categories-message');
    categoryMessage.textContent = 'We could not load the marketplace. Check your connection and refresh to try again.';
    categoryMessage.hidden = false;
    const featuredMessage = document.getElementById('home-featured-message');
    featuredMessage.textContent = 'We could not load featured vendors just now. Try browsing the marketplace instead.';
    featuredMessage.hidden = false;
    showToast('error', 'We could not load the latest vendors', 'Please check your connection and refresh to try again.');
  }
}

loadHome();

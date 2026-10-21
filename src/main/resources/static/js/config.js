export const API_BASE_URL = '/api/v1';

export const STORAGE_KEYS = {
  TOKEN: 'eventsaathi_token',
  USER: 'eventsaathi_user',
  SELECTED_CITY_ID: 'eventsaathi_city_id',
  SELECTED_CITY_NAME: 'eventsaathi_city_name',
  REDIRECT_URL: 'eventsaathi_redirect_url',
  SESSION_FLASH: 'eventsaathi_flash_message'
};

export const ROLES = {
  CUSTOMER: 'ROLE_CUSTOMER',
  VENDOR: 'ROLE_VENDOR',
  ADMIN: 'ROLE_ADMIN'
};

export const EVENT_TYPES = [
  'Wedding',
  'Engagement',
  'Haldi',
  'Mehendi',
  'Sangeet',
  'Birthday',
  'Anniversary',
  'Baby Shower',
  'Corporate',
  'Housewarming'
];

export const FALLBACK_VENDOR_IMAGE = '/assets/images/vendor-placeholder.svg';

export const API_ENDPOINTS = {
  login: '/auth/login',
  register: '/auth/register',
  currentUser: '/auth/me',
  cities: '/cities',
  categories: '/categories',
  eventTypes: '/event-types',
  vendors: '/vendors',
  events: '/events',
  bookings: '/bookings',
  myBookings: '/bookings/mine',
  vendorBookings: '/vendor/bookings',
  recommendations: '/recommendations',
  pendingVendors: '/admin/vendors/pending',
  notifications: '/notifications'
};

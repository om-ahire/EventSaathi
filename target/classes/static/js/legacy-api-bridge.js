/**
 * Temporary bridge for the original single-page prototype while its views are
 * replaced with ES modules. All HTTP requests still go through api.js.
 */
import { api } from './api.js';
import { API_BASE_URL, STORAGE_KEYS } from './config.js';

window.EventSaathiClient = Object.freeze({ api, API_BASE_URL, STORAGE_KEYS });

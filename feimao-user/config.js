const developmentDefault = 'http://127.0.0.1:8080'
const configuredBase = process.env.VUE_APP_API_BASE_URL || developmentDefault

// VUE_APP_API_BASE_URL should be the HTTPS domain registered for the mini-program.
export const API_BASE_URL = configuredBase.replace(/\/$/, '')

import type { AxiosRequestConfig } from 'axios'
import axios from 'axios'

const baseURL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080'

export const api = axios.create({
  baseURL,
  headers: { 'Content-Type': 'application/json' },
})

/**
 * Orval mutator: runs each generated request through our axios instance and returns response data.
 */
export const customInstance = async <T>(
  config: AxiosRequestConfig,
  options?: AxiosRequestConfig
): Promise<T> => {
  const { data } = await api.request<T>({ ...config, ...options })
  return data
}

export default api

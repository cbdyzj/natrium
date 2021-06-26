const NANO_API_BASE = import.meta.env.SNOWPACK_PUBLIC_NANO_API_BASE || new URL(location).origin

export function withNanoApi(endpoint) {
    return new URL(endpoint, NANO_API_BASE).toString()
}

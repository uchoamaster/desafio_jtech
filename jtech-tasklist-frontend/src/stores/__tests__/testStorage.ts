export function installTestStorage() {
  const storage = new Map<string, string>()

  const localStorageMock = {
    clear() {
      storage.clear()
    },
    getItem(key: string) {
      return storage.get(key) ?? null
    },
    key(index: number) {
      return Array.from(storage.keys())[index] ?? null
    },
    removeItem(key: string) {
      storage.delete(key)
    },
    setItem(key: string, value: string) {
      storage.set(key, value)
    },
    get length() {
      return storage.size
    },
  }

  Object.defineProperty(window, 'localStorage', {
    configurable: true,
    value: localStorageMock,
  })

  return localStorageMock
}
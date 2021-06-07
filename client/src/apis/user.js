export async function getUser(token) {
    const response = await fetch('/api/user/user', {
        cache: 'no-cache',
        headers: {
            'X-Token': token
        },
    })
    const result = await response.json()
    if (result.error && response.status !== 403) {
        throw new Error(result.error)
    }
    return result.payload
}

export async function getUserList(token) {
    const response = await fetch('/api/user/list', {
        method: 'GET',
        headers: { 'X-Token': token },
    })
    const result = await response.json()
    if (result.error) {
        throw new Error(result.error)
    }
    return result.payload
}

export async function getChatList(token) {
    const response = await fetch('/api/telegram/chat/list', {
        method: 'GET',
        headers: { 'X-Token': token },
    })
    const result = await response.json()
    if (result.error) {
        throw new Error(result.error)
    }
    return result.payload
}

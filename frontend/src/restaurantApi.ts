export type Restaurant = {
    id: string
    name: string
}

export async function getRestaurants(): Promise<Restaurant[]> {
    const response = await fetch('/api/restaurants')

    if (!response.ok) {
        throw new Error(`Restaurants konnten nicht geladen werden (${response.status})`)
    }

    return response.json() as Promise<Restaurant[]>
}

export async function createRestaurant(name: string): Promise<Restaurant> {
    const response = await fetch('/api/restaurants', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({name}),
    })

    if (!response.ok) {
        throw new Error(`Restaurant konnte nicht gespeichert werden (${response.status})`)
    }

    return response.json() as Promise<Restaurant>
}
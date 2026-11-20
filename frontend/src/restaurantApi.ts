import axios from 'axios'

export type RestaurantLocation = {
    type: 'Point'
    coordinates: [number, number]
}

export type RestaurantFormData = {
    name: string
    cuisines: string[]
    website: string
    street: string
    buildingNumber: string
    postcode: string
    city: string
    openingHours: string
}

export type Restaurant = {
    id: string
    name: string
    cuisines: string[] | null
    website: string | null
    street: string | null
    buildingNumber: string | null
    postcode: string | null
    city: string | null
    openingHours: string | null
    location?: RestaurantLocation | null
}

export function getRestaurants(): Promise<Restaurant[]> {
    return axios.get<Restaurant[]>('/api/restaurants')
        .then((response) => response.data)
}

export function getRestaurantById(id: string): Promise<Restaurant> {
    return axios.get<Restaurant>(
        `/api/restaurants/${encodeURIComponent(id)}`
    ).then((response) => response.data)
}

export function createRestaurant(data: RestaurantFormData): Promise<Restaurant> {
    return axios.post<Restaurant>('/api/restaurants', data)
        .then((response) => response.data)
}

export function updateRestaurant(
    id: string,
    data: RestaurantFormData
): Promise<Restaurant> {
    return axios.put<Restaurant>(
        `/api/restaurants/${encodeURIComponent(id)}`,
        data
    ).then((response) => response.data)
}

export function deleteRestaurant(id: string): Promise<void> {
    return axios.delete(`/api/restaurants/${encodeURIComponent(id)}`)
        .then(() => undefined)
}
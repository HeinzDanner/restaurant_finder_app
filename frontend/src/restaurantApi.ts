import axios from 'axios'

export type RestaurantLocation = {
    type: 'Point'
    coordinates: [number, number]
}

export type Restaurant = {
    id: string
    name: string
    cuisine: string
    location?: RestaurantLocation | null
}

export function getRestaurants(): Promise<Restaurant[]> {
    return axios.get<Restaurant[]>('/api/restaurants')
        .then((response) => response.data)
}

export function createRestaurant(name: string): Promise<Restaurant> {
    return axios.post<Restaurant>('/api/restaurants', {name})
        .then((response) => response.data)
}
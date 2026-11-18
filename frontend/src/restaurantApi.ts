import axios from 'axios'

export type Restaurant = {
    id: string
    name: string
    cuisine: string
}

export function getRestaurants(): Promise<Restaurant[]> {
    return axios.get<Restaurant[]>('/api/restaurants')
        .then((response) => response.data)
}

export function createRestaurant(name: string): Promise<Restaurant> {
    return axios.post<Restaurant>('/api/restaurants', {name})
        .then((response) => response.data)
}
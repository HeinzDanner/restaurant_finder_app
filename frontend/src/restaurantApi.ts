import axios from 'axios'

export type Restaurant = {
    id: string
    name: string
    cuisines: string[],
    website: string,
    street: string,
    buildingNumber: string,
    postcode: string,
    city: string
}

export function getRestaurants(): Promise<Restaurant[]> {
    return axios.get<Restaurant[]>('/api/restaurants')
        .then((response) => response.data)
}

export function createRestaurant(restaurant: Restaurant): Promise<Restaurant> {
    return axios.post<Restaurant>('/api/restaurants', restaurant)
        .then((response) => response.data)
}
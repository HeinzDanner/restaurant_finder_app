import {getRestaurants, type Restaurant} from '../restaurantApi'
import {useEffect, useState} from "react";
import RestaurantPreview from "./RestaurantPreview.tsx";
import {useNavigate} from "react-router-dom";


export default function WelcomePage() {

    const isAdmin = true
    const nav = useNavigate()

    const [restaurants, setRestaurants] = useState<Restaurant[]>([])
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState('')
    const [searchText, setSearchText] = useState("")

    const filteredRestaurants = restaurants.filter((restaurant) =>
        restaurant.name.toLowerCase().includes(searchText.trim().toLowerCase())
    );

    useEffect(() => {
        getRestaurants()
            .then(setRestaurants)
            .catch(() => {
                setError('Restaurants konnten nicht geladen werden.')
            })
            .finally(() => setLoading(false))
    }, [])

    return (
        <>
            <h1>Welcome to Restaurant finder</h1>
            <input type={"text"}
                   onChange={(e) => setSearchText(e.target.value)}
                   placeholder={"search for a restaurant"}/>

            {isAdmin && <button onClick={() => nav("/addNewRestaurant")}> Add new restaurant </button>}
            {loading ? (
                <p>Restaurants werden geladen ...</p>
            ) : error ? (
                <p role="alert">{error}</p>
            ) : restaurants.length === 0 ? (
                <p>Noch keine Restaurants vorhanden.</p>
            ) : filteredRestaurants.length === 0 ? (
                <p>Keiene passenden Restaurants gefunden</p>
            ) : (
                filteredRestaurants.map((restaurant) => (
                    <RestaurantPreview
                        key={restaurant.id}
                        restaurant={restaurant}
                    />
                ))
            )
            }
        </>
    )
}
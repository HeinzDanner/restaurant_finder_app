import {getRestaurants, type Restaurant} from '../restaurantApi'
import {useEffect, useState} from "react";
import RestaurantPreview from "./RestaurantPreview.tsx";
import {useNavigate} from "react-router-dom";
import '../WelcomePage.css'

export default function WelcomePage() {

    const isAdmin = true
    const nav = useNavigate()

    const [restaurants, setRestaurants] = useState<Restaurant[]>([])
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState('')

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

            {isAdmin && <button onClick={() => nav("/addNewRestaurant")}> Add restaurant </button>}
            {loading ? (
                <p>Restaurants werden geladen ...</p>
            ) : error ? (
                <p role="alert">{error}</p>
            ) : restaurants.length === 0 ? (
                    <p>Noch keine Restaurants vorhanden.</p>
                ) :
                (<section className="restaurant-grid">
                        {restaurants.map((restaurant) => (
                            <RestaurantPreview key={restaurant.id} restaurantName={restaurant.name}/>
                        ))}
                    </section>
                )
            }
        </>
    )

}
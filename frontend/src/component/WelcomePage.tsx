import {getRestaurants, type Restaurant} from '../restaurantApi'
import {useEffect, useState} from "react";
import RestaurantPreview from "./RestaurantPreview.tsx";
import {useNavigate} from "react-router-dom";
import '../WelcomePage.css'

export default function WelcomePage() {

    const [now, setNow] = useState(new Date())

    useEffect(() => {
        const timer = setInterval(() =>
            setNow(new Date()), 60000)   // jede Minute
        return () => clearInterval(timer)  // aufräumen beim Verlassen der Seite
    }, [])

    const isAdmin = true
    const nav = useNavigate()

    const [restaurants, setRestaurants] = useState<Restaurant[]>([])
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState('')
    const [searchText, setSearchText] = useState("")
    const [selectedCuisine, setSelectedCuisine] = useState("")

    const filteredRestaurants = restaurants.filter((restaurant) =>
        restaurant.name.toLowerCase().includes(searchText.trim().toLowerCase()) &&
        (selectedCuisine === "" || restaurant.cuisines?.includes(selectedCuisine)));

    useEffect(() => {
        getRestaurants()
            .then(setRestaurants)
            .catch(() => {
                setError('Restaurants konnten nicht geladen werden.')
            })
            .finally(() => setLoading(false))
    }, [])

    function renderRestaurants() {
        if (loading) {
            return <p>Restaurants werden geladen ...</p>
        }
        if (error) {
            return <p role="alert">{error}</p>
        }
        if (restaurants.length === 0) {
            return <p>Noch keine Restaurants vorhanden.</p>
        }
        if (filteredRestaurants.length === 0) {
            return <p>Keine passenden Restaurants gefunden</p>
        }
        return (
            <section className="restaurant-grid">
                {filteredRestaurants.map((restaurant) => (
                    <RestaurantPreview
                        key={restaurant.id}
                        restaurant={restaurant}
                        now={now}
                    />
                ))}
            </section>
        )
    }

    return (
        <>
            <h1>Welcome to Restaurant finder</h1>

            {isAdmin &&
                <button className={"add-button"} onClick={() => nav("/addNewRestaurant")}> Add new restaurant </button>}

            <section className="search-bar">
                <input type={"text"}
                       onChange={(e) => setSearchText(e.target.value)}
                       placeholder={"search for a restaurant"}/>

                <select value={selectedCuisine} onChange={(e) => setSelectedCuisine(e.target.value)}>
                    <option value="">Alle Küchen</option>
                    <option value="italienisch">Italienisch</option>
                    <option value="japanisch">Japanisch</option>
                    <option value="amerikanisch">Amerikanisch</option>
                    <option value="indisch">Indisch</option>
                    <option value="orientalisch">Orientalisch</option>
                    <option value="mexikanisch">Mexikanisch</option>
                    <option value="vegetarisch">Vegetarisch</option>
                    <option value="schwäbisch">Schwäbisch</option>
                    <option value="asiatisch">Asiatisch</option>
                    <option value="griechisch">Griechisch</option>
                    <option value="spanisch">Spanisch</option>
                    <option value="türkisch">Türkisch</option>
                </select>
            </section>
            {renderRestaurants()}
        </>
    )
}
import {type FormEvent, useEffect, useState} from 'react'
import {createRestaurant, getRestaurants, type Restaurant} from './restaurantApi'

function App() {
    const [restaurants, setRestaurants] = useState<Restaurant[]>([])
    const [name, setName] = useState('')
    const [error, setError] = useState('')

    useEffect(() => {
        getRestaurants()
            .then(setRestaurants)
            .catch(() => setError('Restaurants konnten nicht geladen werden.'))
    }, [])

    async function handleSubmit(event: FormEvent<HTMLFormElement>) {
        event.preventDefault()
        setError('')

        try {
            const created = await createRestaurant(name)
            setRestaurants((current) => [...current, created])
            setName('')
        } catch {
            setError('Restaurant konnte nicht gespeichert werden.')
        }
    }

    return (
        <main>
            <h1>Restaurant Finder</h1>

            <form onSubmit={handleSubmit}>
                <label htmlFor="restaurant-name">Restaurantname</label>
                <input
                    id="restaurant-name"
                    value={name}
                    onChange={(event) => setName(event.target.value)}
                    required
                />
                <button type="submit">Hinzufügen</button>
            </form>

            {error && <p role="alert">{error}</p>}

            <h2>Restaurants</h2>
            {restaurants.length === 0 ? (
                <p>Noch keine Restaurants vorhanden.</p>
            ) : (
                <ul>
                    {restaurants.map((restaurant) => (
                        <li key={restaurant.id}>
                            {restaurant.name} <small>({restaurant.id})</small>
                        </li>
                    ))}
                </ul>
            )}
        </main>
    )
}

export default App
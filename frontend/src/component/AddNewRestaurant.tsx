import {type FormEvent, useEffect, useState} from 'react'
import {createRestaurant, getRestaurants, type Restaurant} from '../restaurantApi'
import {useNavigate} from "react-router-dom";

export default function AddNewRestaurant() {

    const nav = useNavigate();
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
            nav("/")
        } catch {
            setError('Restaurant konnte nicht gespeichert werden.')
        }
    }

    return (
        <section>
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
        </section>
    )
}
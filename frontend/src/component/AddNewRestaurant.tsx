import { type FormEvent, useState } from 'react'
import { createRestaurant } from '../restaurantApi'
import {useNavigate} from "react-router-dom";

export default function AddNewRestaurant() {

    const nav = useNavigate();
    const [name, setName] = useState('')
    const [error, setError] = useState('')
    
    async function handleSubmit(event: FormEvent<HTMLFormElement>) {
        event.preventDefault()
        setError('')

        try {
            await createRestaurant(name)
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

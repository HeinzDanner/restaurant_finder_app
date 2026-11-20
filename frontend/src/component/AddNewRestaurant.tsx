import {type FormEvent, useState} from 'react'
import {createRestaurant} from '../restaurantApi'
import {useNavigate} from "react-router-dom";
import type {Restaurant} from "../restaurantApi.ts";
import '../AddNewRestaurant.css'

export default function AddNewRestaurant() {

    const nav = useNavigate();
    const [restaurant, setRestaurant] = useState<Restaurant>({
        id: '',
        name: '',
        cuisines: [],
        website: '',
        street: '',
        buildingNumber: '',
        postcode: '',
        city: '',
        openingHours: ''
    })
    const cuisines = [
        'italienisch',
        'japanisch',
        'amerikanisch',
        'indisch',
        'orientalisch',
        'mexikanisch',
        'vegetarisch',
        'schwäbisch',
        'asiatisch',
        'griechisch',
        'spanisch',
        'türkisch'
    ]

    const [error, setError] = useState('')

    function handleSubmit(event: FormEvent<HTMLFormElement>) {
        event.preventDefault()
        setError('')
        createRestaurant(restaurant)
            .then(() => nav("/"))
            .catch(e => {
                console.log(e);
                setError('Restaurant konnte nicht gespeichert werden.')
            })
    }

    return (
        <section>
            <form onSubmit={handleSubmit}>
                <label htmlFor="restaurant-name">Restaurantname</label>
                <input
                    id="restaurant-name"
                    value={restaurant.name}
                    onChange={(event) => setRestaurant({
                        ...restaurant,
                        name: event.target.value
                    })}
                    required
                />
                <label htmlFor="website">Website</label>
                <input
                    id="website"
                    type="url"
                    value={restaurant.website}
                    onChange={(event) => setRestaurant({
                        ...restaurant,
                        website: event.target.value
                    })}
                />
                <h3>Adresse</h3>

                <label htmlFor="street">Straße</label>
                <input
                    id="street"
                    value={restaurant.street}
                    onChange={(event) => setRestaurant({
                        ...restaurant,
                        street: event.target.value
                    })}
                />

                <label htmlFor="buildingNumber">Hausnummer</label>
                <input
                    id="buildingNumber"
                    value={restaurant.buildingNumber}
                    onChange={(event) => setRestaurant({
                        ...restaurant,
                        buildingNumber: event.target.value
                    })}
                />

                <label htmlFor="postcode">Postleitzahl</label>
                <input
                    id="postcode"
                    value={restaurant.postcode}
                    onChange={(event) => setRestaurant({
                        ...restaurant,
                        postcode: event.target.value
                    })}
                />

                <label htmlFor="city">Stadt</label>
                <input
                    id="city"
                    value={restaurant.city}
                    onChange={(event) => setRestaurant({
                        ...restaurant,
                        city: event.target.value
                    })}
                />
                <h3>Küche auswählen:</h3>

                {cuisines.map((cuisine) => (
                    <label key={cuisine}>
                        <input
                            type="checkbox"
                            value={cuisine}
                            checked={restaurant.cuisines.includes(cuisine)}
                            onChange={(e) => {
                                if (e.target.checked) {
                                    setRestaurant({
                                        ...restaurant,
                                        cuisines: restaurant.cuisines.concat(cuisine)
                                    })
                                } else {
                                    setRestaurant({
                                        ...restaurant,
                                        cuisines: restaurant.cuisines.filter(c => c !== cuisine)
                                    })
                                }
                            }}
                        />
                        {cuisine}
                    </label>
                ))}
                <label htmlFor="openingHours">Öffnungszeiten</label>
                <input
                    id="openingHours"
                    type="text"
                    placeholder="Mo-Fr 09:00-18:00, Sa 10:00-14:00"
                    value={restaurant.openingHours}
                    onChange={(event) => setRestaurant({
                        ...restaurant,
                        openingHours: event.target.value
                    })}
                />
                <button type="submit">Hinzufügen</button>

                <button type="button" onClick={() => nav("/")}>Abbrechen</button>

            </form>
            {error && <p role="alert">{error}</p>}
        </section>
    )
}

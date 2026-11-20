import {useEffect, useState, type FormEvent} from 'react'
import {useNavigate, useParams} from 'react-router-dom'
import {
    getRestaurantById,
    updateRestaurant,
    type Restaurant,
    type RestaurantFormData
} from '../restaurantApi'
import '../AddNewRestaurant.css'

const availableCuisines = [
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

function toFormData(restaurant: Restaurant): RestaurantFormData {
    return {
        name: restaurant.name,
        cuisines: restaurant.cuisines ?? [],
        website: restaurant.website ?? '',
        street: restaurant.street ?? '',
        buildingNumber: restaurant.buildingNumber ?? '',
        postcode: restaurant.postcode ?? '',
        city: restaurant.city ?? '',
        openingHours: restaurant.openingHours ?? ''
    }
}

function hasAddressChanged(
    restaurant: Restaurant,
    formData: RestaurantFormData
): boolean {
    return (
        (restaurant.street ?? '').trim() !== formData.street.trim() ||
        (restaurant.buildingNumber ?? '').trim() !== formData.buildingNumber.trim() ||
        (restaurant.postcode ?? '').trim() !== formData.postcode.trim() ||
        (restaurant.city ?? '').trim() !== formData.city.trim()
    )
}

export default function EditRestaurant() {
    const {id} = useParams()
    const navigate = useNavigate()

    const [restaurant, setRestaurant] = useState<Restaurant | null>(null)
    const [formData, setFormData] = useState<RestaurantFormData | null>(null)
    const [error, setError] = useState('')
    const [saving, setSaving] = useState(false)

    useEffect(() => {
        if (!id) {
            setError('Restaurant-ID fehlt.')
            return
        }

        let active = true

        getRestaurantById(id)
            .then((result) => {
                if (active) {
                    setRestaurant(result)
                    setFormData(toFormData(result))
                }
            })
            .catch(() => {
                if (active) {
                    setError('Restaurant konnte nicht geladen werden.')
                }
            })

        return () => {
            active = false
        }
    }, [id])

    async function handleSubmit(event: FormEvent<HTMLFormElement>) {
        event.preventDefault()

        if (!id || !restaurant || !formData) {
            return
        }

        setError('')
        setSaving(true)

        const addressChanged = hasAddressChanged(restaurant, formData)

        try {
            const updated = await updateRestaurant(id, formData)

            navigate('/restaurantDetails', {
                state: {
                    ...updated,
                    geocodingWarning: addressChanged && !updated.location
                }
            })
        } catch {
            setError('Restaurant konnte nicht gespeichert werden.')
            setSaving(false)
        }
    }

    if (!restaurant || !formData) {
        return error
            ? <p role="alert">{error}</p>
            : <p>Restaurant wird geladen ...</p>
    }

    return (
        <section>
            <h2>Restaurant bearbeiten</h2>

            <form onSubmit={handleSubmit}>
                <label htmlFor="edit-name">Restaurantname</label>
                <input
                    id="edit-name"
                    value={formData.name}
                    onChange={(event) => setFormData({
                        ...formData,
                        name: event.target.value
                    })}
                    required
                />

                <label htmlFor="edit-website">Website</label>
                <input
                    id="edit-website"
                    type="url"
                    value={formData.website}
                    onChange={(event) => setFormData({
                        ...formData,
                        website: event.target.value
                    })}
                />

                <h3>Adresse</h3>

                <label htmlFor="edit-street">Straße</label>
                <input
                    id="edit-street"
                    value={formData.street}
                    onChange={(event) => setFormData({
                        ...formData,
                        street: event.target.value
                    })}
                />

                <label htmlFor="edit-buildingNumber">Hausnummer</label>
                <input
                    id="edit-buildingNumber"
                    value={formData.buildingNumber}
                    onChange={(event) => setFormData({
                        ...formData,
                        buildingNumber: event.target.value
                    })}
                />

                <label htmlFor="edit-postcode">Postleitzahl</label>
                <input
                    id="edit-postcode"
                    value={formData.postcode}
                    onChange={(event) => setFormData({
                        ...formData,
                        postcode: event.target.value
                    })}
                />

                <label htmlFor="edit-city">Stadt</label>
                <input
                    id="edit-city"
                    value={formData.city}
                    onChange={(event) => setFormData({
                        ...formData,
                        city: event.target.value
                    })}
                />

                <h3>Küche auswählen</h3>

                {availableCuisines.map((cuisine) => (
                    <label key={cuisine}>
                        <input
                            type="checkbox"
                            checked={formData.cuisines.includes(cuisine)}
                            onChange={(event) => {
                                const checked = event.target.checked

                                setFormData({
                                    ...formData,
                                    cuisines: checked
                                        ? [...formData.cuisines, cuisine]
                                        : formData.cuisines.filter(
                                            (item) => item !== cuisine
                                        )
                                })
                            }}
                        />
                        {cuisine}
                    </label>
                ))}

                <label htmlFor="edit-openingHours">Öffnungszeiten</label>
                <input
                    id="edit-openingHours"
                    type="text"
                    placeholder="Mo-Fr 09:00-18:00, Sa 10:00-14:00"
                    value={formData.openingHours}
                    onChange={(event) => setFormData({
                        ...formData,
                        openingHours: event.target.value
                    })}
                />

                <button type="submit" disabled={saving}>
                    {saving ? 'Speichert ...' : 'Änderungen speichern'}
                </button>

                <button
                    type="button"
                    disabled={saving}
                    onClick={() => navigate('/restaurantDetails', {
                        state: restaurant
                    })}
                >
                    Abbrechen
                </button>
            </form>

            {error && <p role="alert">{error}</p>}
        </section>
    )
}
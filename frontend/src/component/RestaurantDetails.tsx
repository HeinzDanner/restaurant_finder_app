import {useState} from 'react'
import {useLocation, useNavigate} from 'react-router-dom'
import {deleteRestaurant, type Restaurant} from '../restaurantApi'
import '../RestaurantDetails.css'

type RestaurantDetailsState = Restaurant & {
    geocodingWarning?: boolean
}

export default function RestaurantDetails() {
    const location = useLocation()
    const navigate = useNavigate()
    const restaurant = location.state as RestaurantDetailsState | null

    const [deleteError, setDeleteError] = useState('')
    const [deleting, setDeleting] = useState(false)

    const coordinates = restaurant?.location?.coordinates

    const directionsUrl = coordinates
        ? `https://www.openstreetmap.org/directions?to=${encodeURIComponent(
            `${coordinates[1]},${coordinates[0]}`
        )}`
        : null

    async function handleDelete() {
        if (!restaurant) {
            return
        }

        const confirmed = window.confirm(
            `"${restaurant.name}" wirklich löschen?`
        )

        if (!confirmed) {
            return
        }

        setDeleteError('')
        setDeleting(true)

        try {
            await deleteRestaurant(restaurant.id)
            navigate('/')
        } catch {
            setDeleteError('Restaurant konnte nicht gelöscht werden.')
            setDeleting(false)
        }
    }

    return (
        <section className="restaurant-details">
            <h2>Restaurantdetails</h2>

            {restaurant ? (
                <>
                    <p><strong>Name:</strong> {restaurant.name}</p>
                    <p><strong>Id:</strong> {restaurant.id}</p>
                    <p><strong>Cuisines:</strong> {restaurant.cuisines?.join(', ')}</p>
                    <p><strong>Website:</strong> {restaurant.website}</p>
                    <p>
                        <strong>Address:</strong> {restaurant.street} {restaurant.buildingNumber},
                        {' '}{restaurant.postcode} {restaurant.city}
                    </p>
                    <p><strong>Opening hours:</strong> {restaurant.openingHours}</p>

                    {restaurant.geocodingWarning && (
                        <p role="status">
                            Adresse gespeichert, aber keine Koordinaten gefunden.
                            Die Routenplanung ist für diese Adresse vorerst nicht verfügbar.
                        </p>
                    )}

                    {directionsUrl ? (
                        <p>
                            <a
                                href={directionsUrl}
                                target="_blank"
                                rel="noopener noreferrer"
                            >
                                Route planen auf OpenStreetMap
                            </a>
                        </p>
                    ) : (
                        <p>
                            Keine Koordinaten vorhanden – Routenplanung nicht möglich.
                        </p>
                    )}

                    <div className="restaurant-actions">
                        <button
                            type="button"
                            disabled={deleting}
                            onClick={() => navigate(
                                `/editRestaurant/${encodeURIComponent(restaurant.id)}`
                            )}
                        >
                            ✏️ Bearbeiten
                        </button>

                        <button
                            type="button"
                            disabled={deleting}
                            onClick={handleDelete}
                        >
                            🗑️ Löschen
                        </button>
                    </div>

                    {deleteError && <p role="alert">{deleteError}</p>}
                </>
            ) : (
                <p>Kein Restaurant ausgewählt.</p>
            )}

            <button
                className="back-button"
                disabled={deleting}
                onClick={() => navigate('/')}
            >
                Zurück
            </button>
        </section>
    )
}
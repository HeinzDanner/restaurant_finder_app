import {useLocation, useNavigate} from 'react-router-dom'
import type {Restaurant} from '../restaurantApi'

export default function RestaurantDetails() {
    const location = useLocation()
    const navigate = useNavigate()
    const restaurant = location.state as Restaurant | null

    const coordinates = restaurant?.location?.coordinates
    const directionsUrl = coordinates
        ? `https://www.openstreetmap.org/directions?to=${encodeURIComponent(
            `${coordinates[1]},${coordinates[0]}`
        )}`
        : null

    return (
        <section>
            <h2>Restaurantdetails</h2>

            {restaurant ? (
                <>
                    <h3>Name</h3>
                    <p>{restaurant.name}</p>

                    <h3>ID</h3>
                    <p>{restaurant.id}</p>

                    {directionsUrl ? (
                        <a
                            href={directionsUrl}
                            target="_blank"
                            rel="noopener noreferrer"
                        >
                            Route planen auf OpenStreetMap
                        </a>
                    ) : (
                        <p>
                            Keine Koordinaten vorhanden – Routenplanung nicht möglich.
                        </p>
                    )}
                </>
            ) : (
                <p>Kein Restaurant ausgewählt.</p>
            )}

            <button onClick={() => navigate('/')}>Zurück</button>
        </section>
    )
}

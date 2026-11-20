import {useLocation, useNavigate} from 'react-router-dom'
import type {Restaurant} from '../restaurantApi'
import '../RestaurantDetails.css'

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
        <section className="restaurant-details">
            <h2>Restaurantdetails</h2>

            {restaurant ? (
                <>
                    <p><strong>Name:</strong> {restaurant.name}</p>
                    <p><strong>Id:</strong> {restaurant.id}</p>
                    <p><strong>Cuisines:</strong> {restaurant.cuisines?.join(", ")}</p>
                    <p><strong>Website:</strong> {restaurant.website}</p>
                    <p><strong>Address:</strong> {restaurant.street} {restaurant.buildingNumber},
                        {restaurant.postcode} {restaurant.city}</p>
                    <p><strong>Opening hours:</strong> {restaurant.openingHours}</p>

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
                </>
            ) : (
                <p>Kein Restaurant ausgewählt.</p>
            )}

            <button className={"back-button"} onClick={() => navigate('/')}>Zurück</button>
        </section>
    )
}

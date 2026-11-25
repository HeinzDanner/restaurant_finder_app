import L from 'leaflet'
import {Circle, MapContainer, Marker, Popup, TileLayer} from 'react-leaflet'
import type {Restaurant, RestaurantLocation, StartPoint} from '../restaurantApi'
import 'leaflet/dist/leaflet.css'
import '../stylesheels/RestaurantMap.css'

type RestaurantMapProps = {
    startPoint: StartPoint
    restaurants: Restaurant[]
    radius: number
    radiusActive: boolean
}

function toMapPosition(location: RestaurantLocation): [number, number] {
    const [longitude, latitude] = location.coordinates
    return [latitude, longitude]
}

const startIcon = L.divIcon({
    className: 'start-map-marker',
    html: '<span></span>',
    iconSize: [24, 24],
    iconAnchor: [12, 12]
})

const restaurantIcon = L.divIcon({
    className: 'restaurant-map-marker',
    html: '<span></span>',
    iconSize: [20, 20],
    iconAnchor: [10, 10]
})

export default function RestaurantMap({
                                          startPoint,
                                          restaurants,
                                          radius,
                                          radiusActive
                                      }: RestaurantMapProps) {
    const center = toMapPosition(startPoint.location)
    const zoom = radius >= 25 ? 9 : radius >= 10 ? 11 : radius >= 5 ? 12 : 14

    return (
        <div className="restaurant-map">
            <MapContainer
                key={`${center[0]}-${center[1]}`}
                center={center}
                zoom={zoom}
                scrollWheelZoom
                className="restaurant-map-container"
            >
                <TileLayer
                    attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap-Mitwirkende</a>'
                    url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                />

                {radiusActive && (
                    <Circle
                        center={center}
                        radius={radius * 1000}
                        pathOptions={{color: '#2563eb', fillOpacity: 0.08}}
                    />
                )}

                <Marker position={center} icon={startIcon}>
                    <Popup>
                        Startadresse: {startPoint.address.street}{' '}
                        {startPoint.address.buildingNumber},{' '}
                        {startPoint.address.postcode} {startPoint.address.city}
                    </Popup>
                </Marker>

                {restaurants.map((restaurant) => {
                    if (!restaurant.location) {
                        return null
                    }

                    return (
                        <Marker
                            key={restaurant.id}
                            position={toMapPosition(restaurant.location)}
                            icon={restaurantIcon}
                        >
                            <Popup>{restaurant.name}</Popup>
                        </Marker>
                    )
                })}
            </MapContainer>
        </div>
    )
}
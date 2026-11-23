import axios from 'axios'
import {useEffect, useState, type FormEvent} from 'react'
import {useNavigate} from 'react-router-dom'
import RestaurantPreview from './RestaurantPreview.tsx'
import RestaurantMap from './RestaurantMap.tsx'
import {distanceInKm} from './distance.ts'
import {
    geocodeStartAddress,
    getRestaurants,
    type Restaurant,
    type StartAddress,
    type StartPoint
} from '../restaurantApi'
import '../WelcomePage.css'

type WelcomePageProps = {
    startPoint: StartPoint | null
    setStartPoint: (point: StartPoint | null) => void
    radius: number
    setRadius: (radius: number) => void
    radiusActive: boolean
    setRadiusActive: (active: boolean) => void
}

const emptyAddress: StartAddress = {
    street: '',
    buildingNumber: '',
    postcode: '',
    city: ''
}

export default function WelcomePage({
                                        startPoint,
                                        setStartPoint,
                                        radius,
                                        setRadius,
                                        radiusActive,
                                        setRadiusActive
                                    }: WelcomePageProps) {
    const navigate = useNavigate()
    const isAdmin = true

    const [now, setNow] = useState(new Date())
    const [restaurants, setRestaurants] = useState<Restaurant[]>([])
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState('')
    const [searchText, setSearchText] = useState('')
    const [selectedCuisine, setSelectedCuisine] = useState('')

    const [address, setAddress] = useState<StartAddress>(
        startPoint?.address ?? emptyAddress
    )
    const [addressError, setAddressError] = useState('')
    const [searchingAddress, setSearchingAddress] = useState(false)
    const [showMap, setShowMap] = useState(false)

    useEffect(() => {
        const timer = setInterval(() => setNow(new Date()), 60000)
        return () => clearInterval(timer)
    }, [])

    useEffect(() => {
        getRestaurants()
            .then(setRestaurants)
            .catch(() => {
                setError('Restaurants konnten nicht geladen werden.')
            })
            .finally(() => setLoading(false))
    }, [])

    async function handleAddressSearch(event: FormEvent<HTMLFormElement>) {
        event.preventDefault()
        setAddressError('')

        if (
            !address.street.trim() ||
            (!address.city.trim() && !address.postcode.trim())
        ) {
            setAddressError('Bitte Straße und Stadt oder Postleitzahl eingeben.')
            return
        }

        const submittedAddress: StartAddress = {
            street: address.street.trim(),
            buildingNumber: address.buildingNumber.trim(),
            postcode: address.postcode.trim(),
            city: address.city.trim()
        }

        setSearchingAddress(true)

        try {
            const location = await geocodeStartAddress(submittedAddress)
            setAddress(submittedAddress)
            setStartPoint({address: submittedAddress, location})
            setRadiusActive(true)
        } catch (requestError) {
            if (
                axios.isAxiosError(requestError) &&
                requestError.response?.status === 404
            ) {
                setAddressError('Diese Adresse wurde nicht gefunden.')
            } else {
                setAddressError(
                    'Die Adresse konnte nicht gesucht werden. Bitte später erneut versuchen.'
                )
            }
        } finally {
            setSearchingAddress(false)
        }
    }

    function clearStartPoint() {
        setStartPoint(null)
        setRadiusActive(false)
        setAddress(emptyAddress)
        setAddressError('')
        setShowMap(false)
    }

    const filteredRestaurants = restaurants.filter((restaurant) => {
        const matchesName = restaurant.name
            .toLowerCase()
            .includes(searchText.trim().toLowerCase())

        const matchesCuisine =
            selectedCuisine === '' ||
            restaurant.cuisines?.includes(selectedCuisine) === true

        if (!matchesName || !matchesCuisine) {
            return false
        }

        if (!startPoint || !radiusActive) {
            return true
        }

        if (!restaurant.location) {
            return false
        }

        return distanceInKm(startPoint.location, restaurant.location) <= radius
    })

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
            return <p>Keine passenden Restaurants gefunden.</p>
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

            {isAdmin && (
                <button
                    className="add-button"
                    onClick={() => navigate('/addNewRestaurant')}
                >
                    Add new restaurant
                </button>
            )}

            <form onSubmit={handleAddressSearch}>
                <h2>Startadresse und Umkreis</h2>

                <label htmlFor="start-street">Straße</label>
                <input
                    id="start-street"
                    value={address.street}
                    onChange={(event) => setAddress({
                        ...address,
                        street: event.target.value
                    })}
                    required
                />

                <label htmlFor="start-building-number">Hausnummer</label>
                <input
                    id="start-building-number"
                    value={address.buildingNumber}
                    onChange={(event) => setAddress({
                        ...address,
                        buildingNumber: event.target.value
                    })}
                />

                <label htmlFor="start-postcode">Postleitzahl</label>
                <input
                    id="start-postcode"
                    value={address.postcode}
                    onChange={(event) => setAddress({
                        ...address,
                        postcode: event.target.value
                    })}
                />

                <label htmlFor="start-city">Stadt</label>
                <input
                    id="start-city"
                    value={address.city}
                    onChange={(event) => setAddress({
                        ...address,
                        city: event.target.value
                    })}
                />

                <label htmlFor="radius">Umkreis</label>
                <select
                    id="radius"
                    value={radius}
                    onChange={(event) => setRadius(Number(event.target.value))}
                >
                    <option value={1}>1 km</option>
                    <option value={5}>5 km</option>
                    <option value={10}>10 km</option>
                    <option value={25}>25 km</option>
                    <option value={50}>50 km</option>
                </select>

                <button type="submit" disabled={searchingAddress}>
                    {searchingAddress
                        ? 'Adresse wird gesucht ...'
                        : 'Umkreis suchen'}
                </button>

                <button
                    type="button"
                    onClick={clearStartPoint}
                    disabled={searchingAddress}
                >
                    Startadresse löschen
                </button>

                {addressError && <p role="alert">{addressError}</p>}
            </form>

            {startPoint && (
                <>
                    <p>
                        Aktiver Start: {startPoint.address.street}{' '}
                        {startPoint.address.buildingNumber},{' '}
                        {startPoint.address.postcode} {startPoint.address.city}.{' '}
                        {radiusActive
                            ? `Umkreis: ${radius} km Luftlinie.`
                            : 'Umkreisfilter ist ausgeschaltet.'}
                    </p>

                    <button
                        type="button"
                        onClick={() => setRadiusActive(!radiusActive)}
                    >
                        {radiusActive
                            ? 'Alle Restaurants anzeigen'
                            : 'Umkreissuche wieder aktivieren'}
                    </button>

                    <button
                        type="button"
                        onClick={() => setShowMap((visible) => !visible)}
                    >
                        {showMap
                            ? 'Karte ausblenden'
                            : 'Restaurants auf Karte anzeigen'}
                    </button>

                    {showMap && (
                        <RestaurantMap
                            startPoint={startPoint}
                            restaurants={filteredRestaurants}
                            radius={radius}
                            radiusActive={radiusActive}
                        />
                    )}
                </>
            )}

            
            <p>
                Adresssuche:{' '}
                <a
                    href="https://www.openstreetmap.org/copyright"
                    target="_blank"
                    rel="noopener noreferrer"
                >
                    © OpenStreetMap-Mitwirkende
                </a>
            </p>

            <section className="search-bar">
                <input
                    type="text"
                    value={searchText}
                    onChange={(event) => setSearchText(event.target.value)}
                    placeholder="search for a restaurant"
                />

                <select
                    value={selectedCuisine}
                    onChange={(event) => setSelectedCuisine(event.target.value)}
                >
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
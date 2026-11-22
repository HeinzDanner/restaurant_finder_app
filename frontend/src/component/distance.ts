import type {RestaurantLocation} from '../restaurantApi'

export function distanceInKm(
    first: RestaurantLocation,
    second: RestaurantLocation
): number {
    const [firstLongitude, firstLatitude] = first.coordinates
    const [secondLongitude, secondLatitude] = second.coordinates

    const radians = (degrees: number) => degrees * Math.PI / 180

    const latitudeDifference = radians(secondLatitude - firstLatitude)
    const longitudeDifference = radians(secondLongitude - firstLongitude)

    const a =
        Math.sin(latitudeDifference / 2) ** 2 +
        Math.cos(radians(firstLatitude)) *
        Math.cos(radians(secondLatitude)) *
        Math.sin(longitudeDifference / 2) ** 2

    const centralAngle = 2 * Math.atan2(
        Math.sqrt(a),
        Math.sqrt(Math.max(0, 1 - a))
    )

    return 6371 * centralAngle
}
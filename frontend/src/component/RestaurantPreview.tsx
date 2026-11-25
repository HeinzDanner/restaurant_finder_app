import {useNavigate} from "react-router-dom";
import type {Restaurant} from "../restaurantApi.ts";
import '../stylesheels/RestaurantPreview.css'
import {hasOpeningHours, isOpenNow} from "./openingHours.ts";

type RestaurantPreviewProps = {
    restaurant: Restaurant,
    now: Date
}


export default function RestaurantPreview(props: Readonly<RestaurantPreviewProps>) {

    const nav = useNavigate();

    const open = isOpenNow(props.restaurant.openingHours, props.now)
    return (
        <section className="restaurant-card">
            <h2>{props.restaurant.name}</h2>
            <button onClick={() => nav("/restaurantDetails",
                {state: props.restaurant})}> Details
            </button>

            {hasOpeningHours(props.restaurant.openingHours) && (
                <strong className={open ? "open-status open" : "open-status closed"}>
                    {open ? "Open" : "Closed"}
                </strong>
            )}
        </section>
    )
}

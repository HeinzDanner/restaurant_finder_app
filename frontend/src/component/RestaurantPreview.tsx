import {useNavigate} from "react-router-dom";
import type {Restaurant} from "../restaurantApi.ts";

type RestaurantPreviewProps = {
    restaurant: Restaurant,
}

export default function RestaurantPreview(props: Readonly<RestaurantPreviewProps>) {
    const nav = useNavigate();

    return (
        <section>
            <h2>{props.restaurant.name}</h2>
            <button onClick={() => nav("/restaurantDetails", {state: props.restaurant})}>
                Details
            </button>
        </section>
    )
}
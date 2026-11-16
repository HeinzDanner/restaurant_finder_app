import {useLocation, useNavigate} from "react-router-dom";
import type {Restaurant} from "../restaurantApi.ts";

export default function RestaurantDetails() {
    const location = useLocation();
    const navigate = useNavigate();
    const restaurant = location.state as Restaurant | null;

    return (
        <section>
            <h2>Restaurant Details</h2>
            {restaurant ? (
                <>
                    <h3>Name</h3>
                    <p>{restaurant.name}</p>
                    <h3>ID</h3>
                    <p>{restaurant.id}</p>
                </>
            ) : (
                <p>Kein Restaurant ausgewählt.</p>
            )}
            <button onClick={() => navigate("/")}>Zurück</button>
        </section>
    )
}
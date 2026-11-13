import axios from "axios";
import {getRestaurants, type Restaurant} from '../restaurantApi'
import {useEffect, useState} from "react";
import RestaurantPreview from "./RestaurantPreview.tsx";
import {useNavigate} from "react-router-dom";


export default function WelcomePage(){

    const isAdmin = true;
    const [restaurants, setRestaurants] = useState<Restaurant[]>([]);
    const nav = useNavigate();

    function getRestaurants() {

        return axios.get("/api/restaurants")
            .then((response) => response.data)
            .catch(error => {
                console.error("Get all restaurants failed:", error);
                alert("Something went wrong requesting all restaurants list!");
            });
    }

    useEffect(() => {
        getRestaurants()
            .then(setRestaurants)
    }, [])

    return (
        <>
        <h1>Welcome to Restaurant finder</h1>

        {isAdmin && <button onClick={() => nav("/addNewRestaurant") }> Add restaurant </button>}
            {restaurants.map((restaurant) => (
                    <RestaurantPreview key={restaurant.id} restaurantName ={restaurant.name} />
                ))}
        </>
    )

}
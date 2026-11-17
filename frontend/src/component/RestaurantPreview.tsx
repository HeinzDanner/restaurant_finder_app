import {useNavigate} from "react-router-dom";

type RestaurantPreviewProps = {
    restaurantName: string,
}

export default function RestaurantPreview(props: Readonly<RestaurantPreviewProps>) {

    const nav = useNavigate();

    return (
        <article>
            <h2>{props.restaurantName}</h2>
            <button onClick={() => nav("/restaurantDetails")}> Details</button>
        </article>
    )
}
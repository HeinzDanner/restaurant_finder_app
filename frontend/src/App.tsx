import {useEffect, useState} from 'react'
import {Route, Routes} from 'react-router-dom'
import WelcomePage from './component/WelcomePage.tsx'
import AddNewRestaurant from './component/AddNewRestaurant.tsx'
import RestaurantDetails from './component/RestaurantDetails.tsx'
import EditRestaurant from './component/EditRestaurant.tsx'
import type {StartPoint} from './restaurantApi'
import ProtectedRoutes from "./ProtectedRoutes.tsx";
import type {AppUser} from "./AppUser.ts";
import axios from "axios";

export default function App() {
    const [startPoint, setStartPoint] = useState<StartPoint | null>(null)
    const [radius, setRadius] = useState(10)
    const [radiusActive, setRadiusActive] = useState(false)
    const [user, setUser] = useState<AppUser | null | undefined>(undefined);

    useEffect(() => {
        axios.get<AppUser>('/api/auth/me')
            .then(res => setUser(res.data))
            .catch(() => setUser(null))
    }, [])

    return (
        <main>
            <h1>Restaurant Finder</h1>

            <Routes>
                <Route
                    path="/"
                    element={
                        <WelcomePage
                            startPoint={startPoint}
                            setStartPoint={setStartPoint}
                            radius={radius}
                            setRadius={setRadius}
                            radiusActive={radiusActive}
                            setRadiusActive={setRadiusActive}
                            user={user}
                        />
                    }
                />

                <Route element={<ProtectedRoutes user={user} role={"USER"}/>}>
                    <Route
                        path="/addNewRestaurant"
                        element={<AddNewRestaurant/>}
                    />

                    <Route
                        path="/editRestaurant/:id"
                        element={<EditRestaurant/>}
                    />
                </Route>

                <Route
                    path="/restaurantDetails"
                    element={<RestaurantDetails startPoint={startPoint} user={user}/>}
                />
            </Routes>

            <footer>
                <p>© 2026 Group 2 | HePaZa</p>
            </footer>
        </main>
    )
}
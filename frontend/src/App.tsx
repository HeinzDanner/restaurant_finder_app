import {useState} from 'react'
import {Route, Routes} from 'react-router-dom'
import WelcomePage from './component/WelcomePage.tsx'
import AddNewRestaurant from './component/AddNewRestaurant.tsx'
import RestaurantDetails from './component/RestaurantDetails.tsx'
import EditRestaurant from './component/EditRestaurant.tsx'
import type {StartPoint} from './restaurantApi'

export default function App() {
    const [startPoint, setStartPoint] = useState<StartPoint | null>(null)
    const [radius, setRadius] = useState(10)
    const [radiusActive, setRadiusActive] = useState(false)

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
                        />
                    }
                />

                <Route
                    path="/addNewRestaurant"
                    element={<AddNewRestaurant/>}
                />

                <Route
                    path="/restaurantDetails"
                    element={<RestaurantDetails startPoint={startPoint}/>}
                />

                <Route
                    path="/editRestaurant/:id"
                    element={<EditRestaurant/>}
                />
            </Routes>

            <footer>
                <p>© 2026 Group 2 | HePaZa</p>
            </footer>
        </main>
    )
}
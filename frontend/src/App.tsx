import {Route, Routes} from 'react-router-dom'
import WelcomePage from './component/WelcomePage.tsx'
import AddNewRestaurant from './component/AddNewRestaurant.tsx'
import RestaurantDetails from './component/RestaurantDetails.tsx'
import EditRestaurant from './component/EditRestaurant.tsx'

export default function App() {
    return (
        <main>
            <h1>Restaurant Finder</h1>

            <Routes>
                <Route path="/" element={<WelcomePage/>}/>
                <Route path="/addNewRestaurant" element={<AddNewRestaurant/>}/>
                <Route path="/restaurantDetails" element={<RestaurantDetails/>}/>
                <Route path="/editRestaurant/:id" element={<EditRestaurant/>}/>
            </Routes>

            <footer>
                <p>© 2026 Group 2 | HePaZa</p>
            </footer>
        </main>
    )
}
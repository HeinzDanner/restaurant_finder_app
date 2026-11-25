import type {AppUser} from "./AppUser.ts";
import {Navigate, Outlet} from "react-router";

type ProtectedRoutesProps = {
    user: AppUser | null | undefined;
    //? Optional
    //AppUser["role"] heißt: „derselbe Typ wie das Feld
    // role in AppUser“     role: "USER" | "ADMIN";
    role?: AppUser["role"];
}


export default function ProtectedRoutes({user, role}: Readonly<ProtectedRoutesProps>) {

    if (user === undefined) {
        return <p>Loading...</p>
    }

    if (user === null) {
        return <Navigate to="/" replace/>
    }

    //„Wenn eine Rolle verlangt wird und der User sie nicht hat, dann umleiten.“
    // Ohne role wird dieser Teil übersprungen, und jeder Eingeloggte darf rein.
    if (role && user.role !== role) {
        return <Navigate to="/" replace/>
    }

    return <Outlet/>
}
export type AppUser = {
    id: string;
    username: string;
    avatarUrl: string;
    role: "USER" | "ADMIN";
}
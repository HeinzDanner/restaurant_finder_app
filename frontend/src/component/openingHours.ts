const DAYS = ["Mo", "Di", "Mi", "Do", "Fr", "Sa", "So"]

function toMinutes(time: string): number {
    const [hours, minutes] = time.split(":")
    return Number(hours) * 60 + Number(minutes)
}

export function hasOpeningHours(openingHours: string): boolean {
    return /\d{2}:\d{2}-\d{2}:\d{2}/.test(openingHours)
}

export function isOpenNow(openingHours: string, now: Date): boolean {
    const today = (now.getDay() + 6) % 7          // Mo = 0 … So = 6
    const nowMinutes = now.getHours() * 60 + now.getMinutes()
    const parts = openingHours.split(", ")        // ["Mo-Fr 11:00-22:00", "Sa-So 12:00-23:00"]

    for (const part of parts) {
        const [dayPart, timePart] = part.split(" ")   // "Mo-Fr", "11:00-22:00"
        if (!timePart || timePart === "geschlossen") continue

        const [fromDay, toDay] = dayPart.split("-")   // "Mo", "Fr"  oder nur "So"
        const start = DAYS.indexOf(fromDay)
        const end = toDay ? DAYS.indexOf(toDay) : start
        if (today < start || today > end) continue

        const [openTime, closeTime] = timePart.split("-")
        return nowMinutes >= toMinutes(openTime) && nowMinutes < toMinutes(closeTime)
    }
    return false
}
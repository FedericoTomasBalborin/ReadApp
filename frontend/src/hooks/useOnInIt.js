import { useEffect } from "react"

export const useOnInit = (initialCallBack) => {
    useEffect(() => {
        if (typeof initialCallBack === "function") {
            initialCallBack()
        }
    }, [])
}
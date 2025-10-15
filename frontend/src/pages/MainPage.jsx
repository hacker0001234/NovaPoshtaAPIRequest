import {useEffect, useState} from "react";
import axios from "axios";

export default function MainPage(){
    const [cities,setCities] = useState([]);
    const [filteredCities,setFilteredCities] = useState([]);
    const [target,setTarget] = useState("");

    const [selectedCity,setSelectedCity] = useState("");
    const [departments,setDepartments] = useState([]);

    useEffect(() => {
      axios.get("http://localhost:8080/get/cities").then(res => setCities(res.data));
    }, []);


    const setCity = (e) =>{
        setTarget(e.target.value);
    }
    useEffect(() => {
        if (target.trim() === "") {
            setFilteredCities([]);
            return;
        }

        const result = cities.filter((city) =>
            city.Description.toLowerCase().includes(target.toLowerCase())
        );

        setFilteredCities(result.slice(0,50));
    }, [target]);


    const changeSelectedCity = (e) => {
        setSelectedCity(e.target.value);
    }

    useEffect(() => {
        if(!selectedCity || selectedCity === "test"){
            setDepartments([]);
            return;
        }

        console.log(selectedCity);
        axios.get(`http://localhost:8080/get/department/${selectedCity}`).then(res => setDepartments(res.data))
    }, [selectedCity]);

    return(
        <div>
            <input type={"text"} onChange={setCity}/>

            {filteredCities &&
            <select onChange={changeSelectedCity}>
                <option value={"test"}>Виберіть ваше місто</option>
                {
                    filteredCities.map((city) =>(
                      <option key={city.Ref} value={city.Ref}>{city.Description}</option>
                    ))
                }
            </select>}

            {departments &&
            <select>
                <option>Виберіть ваше відділення</option>
                {departments.map((department)=>(
                    <option key={department.ref}>{department.description}</option>
                    ))
                }
            </select>
            }
        </div>
    )
}

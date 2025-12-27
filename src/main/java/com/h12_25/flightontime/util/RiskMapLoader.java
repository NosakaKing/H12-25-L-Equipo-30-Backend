package com.h12_25.flightontime.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * RiskMapLoader es un componente de Spring (@Component) encargado de cargar
 * archivos JSON de riesgo (carrier_map.json, airport_map.json, dep_time_map.json).
 * Convierte esos archivos en mapas (Map<String, Float>) que luego son usados
 * por el servicio de predicción para alimentar el modelo ONNX.
 */
@Component
public class RiskMapLoader {

    private static final ObjectMapper mapper = new ObjectMapper();

    // Mapa de códigos IATA -> nombres completos del JSON
    public static final Map<String, String> carrierCodeToName = Map.ofEntries(
            Map.entry("AS", "Alaska Airlines Inc."),
            Map.entry("G4", "Allegiant Air"),
            Map.entry("AA", "American Airlines Inc."),
            Map.entry("MQ", "American Eagle Airlines Inc."),
            Map.entry("EV", "Atlantic Southeast Airlines"),
            Map.entry("OH", "Comair Inc."),
            Map.entry("DL", "Delta Air Lines Inc."),
            Map.entry("9E", "Endeavor Air Inc."),
            Map.entry("F9", "Frontier Airlines Inc."),
            Map.entry("HA", "Hawaiian Airlines Inc."),
            Map.entry("B6", "JetBlue Airways"),
            Map.entry("YV", "Mesa Airlines Inc."),
            Map.entry("YX", "Midwest Airline, Inc."),
            Map.entry("OO", "SkyWest Airlines Inc."),
            Map.entry("WN", "Southwest Airlines Co."),
            Map.entry("NK", "Spirit Air Lines"),
            Map.entry("UA", "United Air Lines Inc.")
    );

    // Mapa de códigos IATA -> nombres completos del JSON
    public static final Map<String, String> airportCodeToName = Map.ofEntries(
            Map.entry("LIT", "Adams Field"),
            Map.entry("ALB", "Albany International"),
            Map.entry("ABQ", "Albuquerque International Sunport"),
            Map.entry("ANC", "Anchorage International"),
            Map.entry("ATL", "Atlanta Municipal"),
            Map.entry("AUS", "Austin - Bergstrom International"),
            Map.entry("BHM", "Birmingham Airport"),
            Map.entry("BOI", "Boise Air Terminal"),
            Map.entry("BDL", "Bradley International"),
            Map.entry("CHS", "Charleston International"),
            Map.entry("MDW", "Chicago Midway International"),
            Map.entry("ORD", "Chicago O'Hare International"),
            Map.entry("CVG", "Cincinnati/Northern Kentucky International"),
            Map.entry("CLE", "Cleveland-Hopkins International"),
            Map.entry("DFW", "Dallas Fort Worth Regional"),
            Map.entry("DAL", "Dallas Love Field"),
            Map.entry("DSM", "Des Moines Municipal"),
            Map.entry("DTW", "Detroit Metro Wayne County"),
            Map.entry("ELP", "El Paso International"),
            Map.entry("FLL", "Fort Lauderdale-Hollywood International"),
            Map.entry("BWI", "Friendship International"),
            Map.entry("MKE", "General Mitchell Field"),
            Map.entry("BUF", "Greater Buffalo International"),
            Map.entry("GSP", "Greenville-Spartanburg"),
            Map.entry("BUR", "Hollywood-Burbank Midpoint"),
            Map.entry("HNL", "Honolulu International"),
            Map.entry("IAH", "Houston Intercontinental"),
            Map.entry("IND", "Indianapolis Muni/Weir Cook"),
            Map.entry("JAX", "Jacksonville International"),
            Map.entry("DAY", "James M Cox/Dayton International"),
            Map.entry("JFK", "John F. Kennedy International"),
            Map.entry("OGG", "Kahului Airport"),
            Map.entry("MCI", "Kansas City International"),
            Map.entry("KOA", "Keahole"),
            Map.entry("GRR", "Kent County"),
            Map.entry("LGA", "LaGuardia"),
            Map.entry("STL", "Lambert-St. Louis International"),
            Map.entry("LIH", "Lihue Airport"),
            Map.entry("BOS", "Logan International"),
            Map.entry("LGB", "Long Beach Daugherty Field"),
            Map.entry("LAX", "Los Angeles International"),
            Map.entry("MSY", "Louis Armstrong New Orleans International"),
            Map.entry("LAS", "McCarran International"),
            Map.entry("MEM", "Memphis International"),
            Map.entry("OAK", "Metropolitan Oakland International"),
            Map.entry("MIA", "Miami International"),
            Map.entry("MSP", "Minneapolis-St Paul International"),
            Map.entry("MYR", "Myrtle Beach International"),
            Map.entry("BNA", "Nashville International"),
            Map.entry("EWR", "Newark Liberty International"),
            Map.entry("ORF", "Norfolk International"),
            Map.entry("XNA", "Northwest Arkansas Regional"),
            Map.entry("ONT", "Ontario International"),
            Map.entry("SNA", "Orange County"),
            Map.entry("MCO", "Orlando International"),
            Map.entry("PBI", "Palm Beach International"),
            Map.entry("PSP", "Palm Springs International"),
            Map.entry("PNS", "Pensacola Regional"),
            Map.entry("PHL", "Philadelphia International"),
            Map.entry("PHX", "Phoenix Sky Harbor International"),
            Map.entry("GSO", "Piedmont Triad International"),
            Map.entry("PIT", "Pittsburgh International"),
            Map.entry("CMH", "Port Columbus International"),
            Map.entry("PDX", "Portland International"),
            Map.entry("PWM", "Portland International Jetport"),
            Map.entry("SJU", "Puerto Rico International"),
            Map.entry("RDU", "Raleigh-Durham International"),
            Map.entry("RNO", "Reno/Tahoe International"),
            Map.entry("RIC", "Richmond International"),
            Map.entry("ROC", "Rochester Monroe County"),
            Map.entry("DCA", "Ronald Reagan Washington National"),
            Map.entry("SMF", "Sacramento International"),
            Map.entry("SLC", "Salt Lake City International"),
            Map.entry("SAT", "San Antonio International"),
            Map.entry("SAN", "San Diego International Lindbergh Fl"),
            Map.entry("SFO", "San Francisco International"),
            Map.entry("SJC", "San Jose International"),
            Map.entry("SAV", "Savannah/Hilton Head International"),
            Map.entry("SEA", "Seattle International"),
            Map.entry("RSW", "Southwest Florida International"),
            Map.entry("GEG", "Spokane International"),
            Map.entry("SDF", "Standiford Field"),
            Map.entry("SYR", "Syracuse Hancock International"),
            Map.entry("TPA", "Tampa International"),
            Map.entry("PVD", "Theodore Francis Green State"),
            Map.entry("TUS", "Tucson International"),
            Map.entry("TUL", "Tulsa International"),
            Map.entry("IAD", "Washington Dulles International"),
            Map.entry("OKC", "Will Rogers World"),
            Map.entry("HOU", "William P Hobby")
    );

    // Mapa de códigos IATA -> nombres completos del JSON
    public static final Map<String, String> depTimeCodeToBlock = Map.ofEntries(
            Map.entry("0001-0559", "0001-0559"),
            Map.entry("0600-0659", "0600-0659"),
            Map.entry("0700-0759", "0700-0759"),
            Map.entry("0800-0859", "0800-0859"),
            Map.entry("0900-0959", "0900-0959"),
            Map.entry("1000-1059", "1000-1059"),
            Map.entry("1100-1159", "1100-1159"),
            Map.entry("1200-1259", "1200-1259"),
            Map.entry("1300-1359", "1300-1359"),
            Map.entry("1400-1459", "1400-1459"),
            Map.entry("1500-1559", "1500-1559"),
            Map.entry("1600-1659", "1600-1659"),
            Map.entry("1700-1759", "1700-1759"),
            Map.entry("1800-1859", "1800-1859"),
            Map.entry("1900-1959", "1900-1959"),
            Map.entry("2000-2059", "2000-2059"),
            Map.entry("2100-2159", "2100-2159"),
            Map.entry("2200-2259", "2200-2259"),
            Map.entry("2300-2359", "2300-2359")
    );

    /**
     * Carga un archivo JSON desde el classpath y lo convierte en un Map<String, Float>.
     *
     * - resourceName: nombre del archivo JSON ubicado en src/main/resources.
     * - Usa el ClassLoader para abrir el archivo como InputStream.
     * - Si no se encuentra el archivo, lanza FileNotFoundException.
     * - Con ObjectMapper (mapper) deserializa el contenido del JSON a un Map<String, Float>,
     *   donde la clave es un String (ej. nombre de aerolínea, aeropuerto, bloque horario)
     *   y el valor es un Float (riesgo asociado).
     *
     * Ejemplo de uso:
     *   Map<String, Float> carrierMap = RiskMapLoader.loadMap("carrier_map.json");
     */
    public static Map<String, Float> loadMap(String resourceName) throws IOException {
        // Buscar el archivo en el classpath (src/main/resources)
        InputStream inputStream = RiskMapLoader.class
                .getClassLoader()
                .getResourceAsStream(resourceName);
        if (inputStream == null) {
            throw new FileNotFoundException("No se encontró " + resourceName + " en el classpath");
        }
        return mapper.readValue(inputStream, new TypeReference<Map<String, Float>>() {});
    }
}

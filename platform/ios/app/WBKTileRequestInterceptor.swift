//
//  WBKTileRequestInterceptor.swift
//  ios
//
//  Created by William Kamp on 12/20/16.
//

import Foundation
import Mapbox

public class WBKTileRequestInterceptor : WBKInterceptor {
    
    let hostName = "http://localhost/"
    
    public init() {
    }
    
    public func host() -> String! {
        return hostName
    }
    
    public func handleRequest(_ url: String!, callback cb: WBKResponse!) {
        let path = url.substring(from: hostName.endIndex)
        let range = path.startIndex..<path.index(path.endIndex, offsetBy: -5)
        let resource = path.substring(with: range)
        switch path {
        case "style.json", "raster_data_source_v8.json", "vector_data_source_v8.json":
            let data = bundleJsonAsData(jsonString: resource)
            DispatchQueue.main.async {
                cb.success(data)
            }
            return;
        default:
            //todo split - '/raster/0/0/0' or '/vector/0/0/0'
            break;
        }
        
        DispatchQueue.main.async {
            cb.failure("unknown")
        }
    }
    
    func bundleJsonAsData(jsonString str: String) -> Data {
        guard let path = Bundle.main.path(forResource: str, ofType: "json") else {
            return Data()
        }
        
        do {
            let urlPath = URL(fileURLWithPath: path)
            return try Data(contentsOf: urlPath)
        } catch {
            return Data()
        }
    }

}

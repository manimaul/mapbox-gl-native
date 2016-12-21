//
//  WBKTileRequestInterceptor.swift
//  ios
//
//  Created by William Kamp on 12/20/16.
//

import Foundation
import Mapbox

public class WBKTileRequestInterceptor : WBKInterceptor {
    
    public init() {
    }
    
    public func host() -> String! {
        return "http://localhost"
    }
    
    public func handleRequest(_ url: String!, callback cb: WBKResponse!) {
        cb.failure("todo")
    }

}

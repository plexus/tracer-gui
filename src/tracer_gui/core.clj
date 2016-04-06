(ns tracer-gui.core
  (:require [tracer-gui.gui :refer [create-tracer-window current-gui]])
  (:import java.lang.Runnable
           [javafx.application Application Platform]
           tracer-gui.FXApp))

(defonce state (atom {}))

(defn launch []
  (Application/launch FXApp (into-array String [])))

(defn open-tracer-gui [state]
  (future
    (Platform/runLater
     (reify Runnable
       (run [_]
         (create-tracer-window state))))))

(defn trace-ring
  ([handler]
   (trace-ring handler "ring"))
  ([handler identifier]
   (defonce launched (future (launch)))
   (swap! state assoc identifier (get @state identifier []))
   (when-not @current-gui
     (reset! current-gui true)
     (open-tracer-gui state))
   (fn [req]
     (let [res (handler req)]
       (swap! state update identifier conj {:request req :response res})
       res))))

(comment
  (clojure.core/compile 'tracer-gui.gui))

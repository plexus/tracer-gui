(ns tracer-gui.gui
  (:gen-class
   :extends javafx.application.Application
   :name tracer-gui.FXApp)
  (:import java.lang.Throwable
           javafx.application.Platform
           javafx.beans.property.SimpleStringProperty
           javafx.beans.value.ChangeListener
           javafx.collections.FXCollections
           javafx.event.EventHandler
           javafx.geometry.Insets
           [javafx.scene.control Tab TableColumn TableView TabPane]
           [javafx.scene.layout GridPane Priority]
           javafx.scene.Scene
           javafx.stage.Stage
           javafx.util.Callback
           javafx.collections.transformation.SortedList))

(def ring-columns
  [["✓"      #(get-in % [:response :status])]
   ["method" #(get-in % [:request :request-method])]
   ["uri"    #(get-in % [:request :uri])] ])

(def map-columns
  [["key" first]
   ["value" last]])

;; Hack so we only open one window at a time
(defonce current-gui (atom nil))

(defn -start
  "The javafx.application.Application start method. We simply make sure JavaFX
  is properly initialized, then we close the main window, we can spawn new
  windows later on."
  [this ^Stage stage]
  (Platform/setImplicitExit false)
  (.show stage)
  (.hide stage))

(defn table-column [name f]
  (doto (TableColumn. name)
    (.setCellValueFactory
     (reify Callback
       (call [_ cell-data]
         (SimpleStringProperty. nil nil (print-str (f (.getValue cell-data)))))))))

(defn create-table [columns]
  (let [table (TableView.)]
    (.setColumnResizePolicy table TableView/CONSTRAINED_RESIZE_POLICY)
    (.addAll
     (.getColumns table)
     (for [[name f] columns] (table-column name f)))
    table))

(defn add-to-grid! [grid node x y]
  (.add (.getChildren grid) node)
  (GridPane/setVgrow node Priority/ALWAYS)
  (GridPane/setHgrow node Priority/ALWAYS)
  (GridPane/setConstraints node x y))

(defn set-table-items! [table items]
  (let [items (SortedList. (FXCollections/observableList items))]
    (.bind (.comparatorProperty items) (.comparatorProperty table))
    (.setItems table items)))

(defn add-table-change-listener! [table f]
  (-> table
      .getSelectionModel
      .selectedItemProperty
      (.addListener
       (reify ChangeListener
         (changed [_ _ old-val new-val]
           (f old-val new-val))))))

(defn add-on-close-handler [stage]
  (.setOnCloseRequest stage
                      (reify EventHandler
                        (handle [_ _]
                          (reset! current-gui nil)))))

(defn create-tracer-pane [state cork columns]
  (let [table (create-table columns)
        req-table (create-table map-columns)
        res-table (create-table map-columns)
        grid (GridPane.) ;; in hindsight maybe a SplitPane would be nicer
        ]

    (.setPadding grid (Insets. 10 10 10 10))

    (add-to-grid! grid table 0 0)
    (add-to-grid! grid req-table 0 1)
    (add-to-grid! grid res-table 0 2)

    (set-table-items! table (@state cork))

    (add-watch state (str cork :gui-watch) (fn [_ _ _ s]
                                             (set-table-items! table (s cork))))

    (add-table-change-listener!
     table
     (fn [_ v]
       (set-table-items! req-table (vec (:request v)))
       (set-table-items! res-table (vec (:response v)))))

    grid))

(defn add-tracer-pane [tabs ])

(defn create-tracer-window [state]
  (try
    (let [stage (Stage.)
          tab-pane (TabPane.)
          scene (Scene. tab-pane)]
      (reset! current-gui stage)
      (doseq [k (keys @state)]
        (.add (.getTabs tab-pane)
         (doto (Tab.)
           (.setText (print-str k))
           (.setContent (create-tracer-pane state k ring-columns)))))
      (doto stage
        (.setTitle "Ring Debugger")
        (.setWidth 800)
        (.setHeight 500)
        (.setScene scene)
        (.show)
        add-on-close-handler))
    (catch Throwable t
      (println t))))

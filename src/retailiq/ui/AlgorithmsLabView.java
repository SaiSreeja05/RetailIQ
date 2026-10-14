package retailiq.ui;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import retailiq.Product;
import retailiq.Review;
import retailiq.service.AlgorithmLabService;
import retailiq.service.RetailIQService;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AlgorithmsLabView extends ScrollPane {

    private final RetailIQService service;
    private final AlgorithmLabService lab;

    private final VBox rootContent = new VBox(20);

    public AlgorithmsLabView(RetailIQService service) {
        this.service = service;
        this.lab = new AlgorithmLabService(service);

        setFitToWidth(true);
        getStyleClass().add("main-container");
        setStyle("-fx-background: #0b0f19; -fx-background-color: transparent;");

        rootContent.setPadding(new Insets(24));
        setContent(rootContent);

        buildHeader();
        buildTabs();
    }

    private void buildHeader() {
        VBox titleBox = new VBox(2);
        Label title = new Label("Algorithms Lab & DSA Interactive Studio");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #f8fafc;");
        Label subtitle = new Label("Explore and visualize RetailIQ's algorithmic engines running directly on live project data");
        subtitle.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");
        titleBox.getChildren().addAll(title, subtitle);
        rootContent.getChildren().add(titleBox);
    }

    private void buildTabs() {
        TabPane tabPane = new TabPane();
        tabPane.getStyleClass().add("tab-pane");

        Tab tabDP = new Tab("  1. Dynamic Programming  ", buildDPTab());
        tabDP.setClosable(false);

        Tab tabStr = new Tab("  2. String Algorithms  ", buildStringTab());
        tabStr.setClosable(false);

        Tab tabSuffix = new Tab("  3. Suffix Structures  ", buildSuffixTab());
        tabSuffix.setClosable(false);

        tabPane.getTabs().addAll(tabDP, tabStr, tabSuffix);
        rootContent.getChildren().add(tabPane);
    }

    // =========================================================================
    // TAB 1: DYNAMIC PROGRAMMING
    // =========================================================================
    private VBox buildDPTab() {
        VBox box = new VBox(20);
        box.setPadding(new Insets(16, 0, 16, 0));

        // 1. Bitmask DP - Live Product Bundle Optimizer
        VBox bitmaskCard = new VBox(12);
        bitmaskCard.getStyleClass().add("card");

        Label bmTitle = new Label("0/1 Knapsack via Bitmask DP — Retail Bundle Optimizer (Real CSV Data)");
        bmTitle.getStyleClass().add("card-header");

        Label bmDesc = new Label("Evaluates all 2^N product combinations to maximize bundle satisfaction within a customer's monetary budget.");
        bmDesc.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px;");

        HBox bmInputRow = new HBox(12);
        bmInputRow.setAlignment(Pos.CENTER_LEFT);

        Label lblBudget = new Label("Budget (Rs.):");
        lblBudget.setStyle("-fx-text-fill: #cbd5e1; -fx-font-weight: bold;");

        Slider budgetSlider = new Slider(10000, 250000, 100000);
        budgetSlider.setPrefWidth(260);
        budgetSlider.setShowTickMarks(true);
        budgetSlider.setShowTickLabels(true);

        Label lblBudgetValue = new Label("Rs. 100,000");
        lblBudgetValue.setStyle("-fx-font-weight: bold; -fx-text-fill: #34d399;");
        budgetSlider.valueProperty().addListener((obs, oldV, newV) -> {
            lblBudgetValue.setText(UIHelper.formatCurrency(newV.intValue()));
        });

        Button btnRunBM = new Button("Run Bitmask Optimizer");
        btnRunBM.getStyleClass().add("btn-primary");

        bmInputRow.getChildren().addAll(lblBudget, budgetSlider, lblBudgetValue, btnRunBM);

        VBox bmResultBox = new VBox(8);
        bmResultBox.setStyle("-fx-background-color: #0f172a; -fx-padding: 12px; -fx-background-radius: 8px;");
        Label bmResultHeader = new Label("Optimization Output:");
        bmResultHeader.setStyle("-fx-font-weight: bold; -fx-text-fill: #f8fafc;");
        Label bmResultText = new Label("Click 'Run Bitmask Optimizer' to calculate.");
        bmResultText.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 12.5px;");
        bmResultText.setWrapText(true);
        FlowPane bmSelectedChips = new FlowPane(8, 8);
        bmResultBox.getChildren().addAll(bmResultHeader, bmResultText, bmSelectedChips);

        btnRunBM.setOnAction(e -> {
            int budget = (int) budgetSlider.getValue();
            AlgorithmLabService.BundleResult res = lab.runBitmaskBundleOptimization(budget);
            bmResultText.setText(res.explanation);
            bmSelectedChips.getChildren().clear();
            for (Product p : res.selectedProducts) {
                Label chip = new Label("✓ " + p.getName() + " (Rs. " + (int) p.getPrice() + ")");
                chip.getStyleClass().add("badge-emerald");
                bmSelectedChips.getChildren().add(chip);
            }
        });

        bitmaskCard.getChildren().addAll(bmTitle, bmDesc, bmInputRow, bmResultBox);

        // 2. Levenshtein Distance Studio
        VBox levCard = new VBox(12);
        levCard.getStyleClass().add("card");

        Label levTitle = new Label("Levenshtein Distance — Typo Tolerance & Fuzzy Search Engine");
        levTitle.getStyleClass().add("card-header");

        HBox levInputRow = new HBox(12);
        levInputRow.setAlignment(Pos.CENTER_LEFT);

        TextField tfS1 = new TextField("ipohne");
        tfS1.setPromptText("String 1 (e.g. user typo)");
        tfS1.setPrefWidth(160);

        TextField tfS2 = new TextField("iPhone 15");
        tfS2.setPromptText("String 2 (e.g. catalog product)");
        tfS2.setPrefWidth(160);

        ComboBox<Product> cbQuickProd = new ComboBox<>();
        cbQuickProd.setPromptText("Quick Pick Product...");
        cbQuickProd.setItems(FXCollections.observableArrayList(service.getAllProducts()));
        cbQuickProd.setCellFactory(lv -> new ListCell<Product>() {
            @Override
            protected void updateItem(Product item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName());
            }
        });
        cbQuickProd.setButtonCell(new ListCell<Product>() {
            @Override
            protected void updateItem(Product item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName());
            }
        });
        cbQuickProd.setOnAction(e -> {
            if (cbQuickProd.getValue() != null) {
                tfS2.setText(cbQuickProd.getValue().getName());
            }
        });

        Button btnRunLev = new Button("Compute Edit Distance");
        btnRunLev.getStyleClass().add("btn-primary");

        levInputRow.getChildren().addAll(new Label("Query:"), tfS1, new Label("Target:"), tfS2, cbQuickProd, btnRunLev);

        VBox levResultBox = new VBox(8);
        levResultBox.setStyle("-fx-background-color: #0f172a; -fx-padding: 12px; -fx-background-radius: 8px;");
        Label levResultText = new Label("Enter two strings and click 'Compute Edit Distance'.");
        levResultText.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 12.5px;");
        levResultText.setWrapText(true);

        GridPane dpGrid = new GridPane();
        dpGrid.setHgap(4);
        dpGrid.setVgap(4);
        ScrollPane dpScroll = new ScrollPane(dpGrid);
        dpScroll.setPrefHeight(160);
        dpScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        btnRunLev.setOnAction(e -> {
            String s1 = tfS1.getText().trim();
            String s2 = tfS2.getText().trim();
            AlgorithmLabService.LevenshteinResult res = lab.runLevenshtein(s1, s2);
            levResultText.setText(res.explanation);

            // Render DP table
            dpGrid.getChildren().clear();
            int n = res.s1.length();
            int m = res.s2.length();

            // Limit visualization size if strings are long
            int maxN = Math.min(n, 15);
            int maxM = Math.min(m, 15);

            for (int r = 0; r <= maxN; r++) {
                for (int c = 0; c <= maxM; c++) {
                    Label cell = new Label(String.valueOf(res.dpTable[r][c]));
                    cell.setAlignment(Pos.CENTER);
                    cell.setPrefSize(28, 28);
                    if (r == maxN && c == maxM) {
                        cell.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 4px;");
                    } else {
                        cell.setStyle("-fx-background-color: #1e293b; -fx-text-fill: #94a3b8; -fx-background-radius: 4px;");
                    }
                    dpGrid.add(cell, c, r);
                }
            }
        });

        levResultBox.getChildren().addAll(levResultText, new Label("Levenshtein 2D DP Matrix Visualizer:"), dpScroll);
        levCard.getChildren().addAll(levTitle, levInputRow, levResultBox);

        // 3. Matrix Chain Multiplication
        VBox mcmCard = new VBox(12);
        mcmCard.getStyleClass().add("card");

        Label mcmTitle = new Label("Matrix Chain Optimization — Supply Chain Restock Multi-Stage Cost");
        mcmTitle.getStyleClass().add("card-header");

        HBox mcmInputRow = new HBox(12);
        mcmInputRow.setAlignment(Pos.CENTER_LEFT);

        TextField tfMcmDims = new TextField("10, 20, 30, 40, 30");
        tfMcmDims.setPrefWidth(220);

        Button btnRunMcm = new Button("Optimize Parenthesization");
        btnRunMcm.getStyleClass().add("btn-primary");

        mcmInputRow.getChildren().addAll(new Label("Stage Dimensions:"), tfMcmDims, btnRunMcm);

        VBox mcmResultBox = new VBox(6);
        mcmResultBox.setStyle("-fx-background-color: #0f172a; -fx-padding: 12px; -fx-background-radius: 8px;");
        Label mcmResultText = new Label("Click 'Optimize Parenthesization' to run.");
        mcmResultText.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 12.5px;");
        mcmResultText.setWrapText(true);
        mcmResultBox.getChildren().add(mcmResultText);

        btnRunMcm.setOnAction(e -> {
            try {
                String[] parts = tfMcmDims.getText().split(",");
                int[] dims = new int[parts.length];
                for (int i = 0; i < parts.length; i++) dims[i] = Integer.parseInt(parts[i].trim());
                AlgorithmLabService.MatrixChainResult res = lab.runMatrixChain(dims);
                mcmResultText.setText(res.explanation);
            } catch (Exception ex) {
                mcmResultText.setText("Invalid dimensions. Enter comma-separated integers (e.g. 10, 20, 30, 40, 30).");
            }
        });

        mcmCard.getChildren().addAll(mcmTitle, mcmInputRow, mcmResultBox);

        box.getChildren().addAll(bitmaskCard, levCard, mcmCard);
        return box;
    }

    // =========================================================================
    // TAB 2: STRING ALGORITHMS
    // =========================================================================
    private VBox buildStringTab() {
        VBox box = new VBox(20);
        box.setPadding(new Insets(16, 0, 16, 0));

        // 1. Aho-Corasick Multi-Keyword Matcher
        VBox acCard = new VBox(12);
        acCard.getStyleClass().add("card");

        Label acTitle = new Label("Aho-Corasick Automaton — Review Sentiment & Feature Mining");
        acTitle.getStyleClass().add("card-header");

        Label acDesc = new Label("Builds a finite-state trie with failure links to scan entire catalog reviews for multiple keywords simultaneously in O(N + M) linear time.");
        acDesc.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px;");

        HBox acQuickRow = new HBox(12);
        acQuickRow.setAlignment(Pos.CENTER_LEFT);

        ComboBox<Review> cbQuickReview = new ComboBox<>();
        cbQuickReview.setPromptText("Load Sample Customer Review from CSV...");
        cbQuickReview.setPrefWidth(320);
        cbQuickReview.setItems(FXCollections.observableArrayList(service.getAllReviews()));
        cbQuickReview.setCellFactory(lv -> new ListCell<Review>() {
            @Override
            protected void updateItem(Review item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : "Review #" + item.getReviewId() + ": " + item.getReviewText());
            }
        });
        cbQuickReview.setButtonCell(new ListCell<Review>() {
            @Override
            protected void updateItem(Review item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : "Review #" + item.getReviewId() + ": " + item.getReviewText());
            }
        });

        TextArea taAcText = new TextArea("The iPhone Pro has an excellent camera and smooth performance with titanium design.");
        taAcText.setPrefRowCount(2);

        cbQuickReview.setOnAction(e -> {
            if (cbQuickReview.getValue() != null) {
                taAcText.setText(cbQuickReview.getValue().getReviewText());
            }
        });

        TextField tfAcKeywords = new TextField("camera, performance, titanium, battery, smooth, design, excellent");
        tfAcKeywords.setPromptText("Comma-separated keywords");

        Button btnRunAc = new Button("Run Aho-Corasick Scanner");
        btnRunAc.getStyleClass().add("btn-primary");

        VBox acResultBox = new VBox(8);
        acResultBox.setStyle("-fx-background-color: #0f172a; -fx-padding: 12px; -fx-background-radius: 8px;");
        Label acResultText = new Label("Click 'Run Aho-Corasick Scanner' to detect keywords.");
        acResultText.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 12.5px;");
        acResultText.setWrapText(true);
        FlowPane acChips = new FlowPane(8, 8);
        acResultBox.getChildren().addAll(acResultText, acChips);

        btnRunAc.setOnAction(e -> {
            String text = taAcText.getText().trim();
            List<String> kws = Arrays.stream(tfAcKeywords.getText().split(","))
                    .map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());
            AlgorithmLabService.KeywordMatchResult res = lab.runAhoCorasick(text, kws);
            acResultText.setText(res.explanation);
            acChips.getChildren().clear();
            for (String kw : res.matchedKeywords) {
                Label chip = new Label("Found: " + kw.toUpperCase());
                chip.getStyleClass().add("badge-emerald");
                acChips.getChildren().add(chip);
            }
        });

        acCard.getChildren().addAll(acTitle, acDesc, cbQuickReview, new Label("Text to Scan:"), taAcText,
                new Label("Target Keywords:"), tfAcKeywords, btnRunAc, acResultBox);

        // 2. KMP, Rabin-Karp & Z-Function Comparison
        VBox matchCard = new VBox(12);
        matchCard.getStyleClass().add("card");

        Label matchTitle = new Label("Pattern Matching Suite: KMP vs Rabin-Karp vs Z-Algorithm");
        matchTitle.getStyleClass().add("card-header");

        HBox matchInputs = new HBox(12);
        matchInputs.setAlignment(Pos.CENTER_LEFT);

        TextField tfText = new TextField("Apple iPhone 15 Pro with titanium design and advanced camera");
        tfText.setPrefWidth(320);
        TextField tfPattern = new TextField("titanium");
        tfPattern.setPrefWidth(120);

        Button btnKMP = new Button("Run KMP");
        btnKMP.getStyleClass().add("btn-secondary");

        Button btnRK = new Button("Run Rabin-Karp");
        btnRK.getStyleClass().add("btn-secondary");

        Button btnZ = new Button("Run Z-Algorithm");
        btnZ.getStyleClass().add("btn-secondary");

        matchInputs.getChildren().addAll(new Label("Catalog Text:"), tfText, new Label("Pattern:"), tfPattern, btnKMP, btnRK, btnZ);

        VBox matchResultBox = new VBox(8);
        matchResultBox.setStyle("-fx-background-color: #0f172a; -fx-padding: 12px; -fx-background-radius: 8px;");
        Label matchResultText = new Label("Select any pattern search algorithm above.");
        matchResultText.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 12.5px;");
        matchResultText.setWrapText(true);
        matchResultBox.getChildren().add(matchResultText);

        btnKMP.setOnAction(e -> {
            AlgorithmLabService.StringSearchResult r = lab.runKMP(tfText.getText().trim(), tfPattern.getText().trim());
            matchResultText.setText(r.explanation);
        });

        btnRK.setOnAction(e -> {
            AlgorithmLabService.StringSearchResult r = lab.runRabinKarp(tfText.getText().trim(), tfPattern.getText().trim());
            matchResultText.setText(r.explanation);
        });

        btnZ.setOnAction(e -> {
            AlgorithmLabService.ZFunctionResult r = lab.runZFunction(tfText.getText().trim(), tfPattern.getText().trim());
            matchResultText.setText(r.explanation);
        });

        matchCard.getChildren().addAll(matchTitle, matchInputs, matchResultBox);

        box.getChildren().addAll(acCard, matchCard);
        return box;
    }

    // =========================================================================
    // TAB 3: SUFFIX STRUCTURES
    // =========================================================================
    private VBox buildSuffixTab() {
        VBox box = new VBox(20);
        box.setPadding(new Insets(16, 0, 16, 0));

        // 1. Suffix Array & Kasai LCP Array
        VBox saCard = new VBox(12);
        saCard.getStyleClass().add("card");

        Label saTitle = new Label("Suffix Array & Kasai LCP Array — Substring Indexing");
        saTitle.getStyleClass().add("card-header");

        HBox saInputRow = new HBox(12);
        saInputRow.setAlignment(Pos.CENTER_LEFT);

        TextField tfSaText = new TextField("retailiq");
        tfSaText.setPrefWidth(200);

        Button btnRunSa = new Button("Construct Suffix & LCP Array");
        btnRunSa.getStyleClass().add("btn-primary");

        saInputRow.getChildren().addAll(new Label("Corpus Text:"), tfSaText, btnRunSa);

        VBox saResultBox = new VBox(8);
        saResultBox.setStyle("-fx-background-color: #0f172a; -fx-padding: 12px; -fx-background-radius: 8px;");
        Label saResultText = new Label("Enter a word and click to generate Suffix & LCP Array.");
        saResultText.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 12.5px;");
        saResultText.setWrapText(true);

        TableView<String> saTable = new TableView<>();
        saTable.setPrefHeight(180);
        TableColumn<String, String> colLine = new TableColumn<>("Suffix Array Entry (Rank, Suffix, LCP)");
        colLine.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue()));
        colLine.setPrefWidth(420);
        saTable.getColumns().add(colLine);

        btnRunSa.setOnAction(e -> {
            String text = tfSaText.getText().trim();
            AlgorithmLabService.SuffixArrayKasaiResult res = lab.runSuffixArrayAndKasai(text);
            saResultText.setText(res.explanation);
            saTable.getItems().clear();
            for (int i = 0; i < res.suffixArray.length; i++) {
                int pos = res.suffixArray[i];
                int lcpVal = res.lcpArray[i];
                String suffixStr = res.suffixStrings.get(i);
                saTable.getItems().add(String.format("Rank %d | Start %d | LCP: %d | Suffix: '%s'", i, pos, lcpVal, suffixStr));
            }
        });

        saResultBox.getChildren().addAll(saResultText, saTable);
        saCard.getChildren().addAll(saTitle, saInputRow, saResultBox);

        // 2. Suffix Automaton & Suffix Tree
        VBox samCard = new VBox(12);
        samCard.getStyleClass().add("card");

        Label samTitle = new Label("Suffix Automaton (DAWG) & Suffix Tree Substring Verifier");
        samTitle.getStyleClass().add("card-header");

        HBox samInputs = new HBox(12);
        samInputs.setAlignment(Pos.CENTER_LEFT);

        TextField tfSamCorpus = new TextField("electronics_catalog_inventory");
        tfSamCorpus.setPrefWidth(240);

        TextField tfSamPattern = new TextField("catalog");
        tfSamPattern.setPrefWidth(120);

        Button btnRunSam = new Button("Query Suffix Automaton");
        btnRunSam.getStyleClass().add("btn-secondary");

        Button btnRunTree = new Button("Query Suffix Tree");
        btnRunTree.getStyleClass().add("btn-secondary");

        samInputs.getChildren().addAll(new Label("Corpus:"), tfSamCorpus, new Label("Pattern:"), tfSamPattern, btnRunSam, btnRunTree);

        VBox samResultBox = new VBox(6);
        samResultBox.setStyle("-fx-background-color: #0f172a; -fx-padding: 12px; -fx-background-radius: 8px;");
        Label samResultText = new Label("Query the Suffix Automaton or Suffix Tree above.");
        samResultText.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 12.5px;");
        samResultText.setWrapText(true);
        samResultBox.getChildren().add(samResultText);

        btnRunSam.setOnAction(e -> {
            AlgorithmLabService.SuffixQueryResult r = lab.runSuffixAutomaton(tfSamCorpus.getText().trim(), tfSamPattern.getText().trim());
            samResultText.setText(r.explanation);
        });

        btnRunTree.setOnAction(e -> {
            AlgorithmLabService.SuffixQueryResult r = lab.runSuffixTree(tfSamCorpus.getText().trim(), tfSamPattern.getText().trim());
            samResultText.setText(r.explanation);
        });

        samCard.getChildren().addAll(samTitle, samInputs, samResultBox);

        box.getChildren().addAll(saCard, samCard);
        return box;
    }
}

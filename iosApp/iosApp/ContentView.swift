import SwiftUI
import shared

struct ContentView: View {

	var body: some View {
        KuiklyRenderViewPage(pageName: "main", data: [:]).ignoresSafeArea()
	}
}

struct ContentView_Previews: PreviewProvider {
	static var previews: some View {
		ContentView()
	}
}
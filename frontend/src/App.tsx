import { BrowserRouter, Routes, Route, Link } from 'react-router-dom'
import PackageList from './pages/PackageList'
import CreatePackage from './pages/CreatePackage'
import PackageDetail from './pages/PackageDetail'

function App() {
  return (
    <BrowserRouter>
      <nav style={{ marginBottom: '1rem' }}>
        <Link to="/" style={{ marginRight: '1rem' }}>Packages</Link>
        <Link to="/create">Create Package</Link>
      </nav>
      <Routes>
        <Route path="/" element={<PackageList />} />
        <Route path="/create" element={<CreatePackage />} />
        <Route path="/packages/:id" element={<PackageDetail />} />
      </Routes>
    </BrowserRouter>
  )
}

export default App
